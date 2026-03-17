package com.terraformersmc.biolith.impl.biomeperimeters;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.terraformersmc.biolith.api.biomeperimeters.BiomePerimeters;
import com.terraformersmc.biolith.impl.Biolith;
import com.terraformersmc.biolith.impl.compat.VanillaCompat;
import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import org.jspecify.annotations.Nullable;

import java.util.Hashtable;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * BiomePerimetersImpl
 * <p></p>
 * This class builds a bidirectional hashed list of the points (voxels) on the perimeter of a biome instance.
 * The points are used to provide an estimate of how far "in-biome" a given biome voxel is.  This can allow
 * biome generation (f.e. surface builders) to blend with surrounding biomes or generate context-sensitive
 * terrain heights within a particular biome.
 * <p></p>
 * Note:  In order to achieve acceptable performance in-game, BiomePerimetersImpl makes heavy use of caching and
 * also accepts certain compromises with respect to the accuracy of the perimeter distance values.  It is safe
 * to call getPerimeterDistance() for every individual block column during generation, but on the other hand,
 * minor discontinuities and variations may occasionally occur in the distance values.
 */
public class BiomePerimetersImpl implements BiomePerimeters {
	private static final Hashtable<Biome, BiomePerimetersImpl> instances = new Hashtable<>(4);

	private final Biome biome;
	private final int cardinalHorizon;
	private final int ordinalHorizon;
	private final int checkDistance;

	public static final int MAX_HORIZON = 256;

	private final LoadingCache<ChunkPos, CacheRecord> caches =
			CacheBuilder.newBuilder()
					.maximumSize(4096)
					.expireAfterAccess(300, TimeUnit.SECONDS)
					.weakValues()
					.build(new CacheLoader<>() {
						@Override
						public CacheRecord load(ChunkPos key) {
							return new CacheRecord();
						}
					});

	private static final int MAX_THREAD_LOCAL_CACHE_SIZE = 1024;
	private final ThreadLocal<Object2ObjectLinkedOpenHashMap<ChunkPos, CacheRecord>> threadLocalCaches =
			ThreadLocal.withInitial(Object2ObjectLinkedOpenHashMap::new);

	private CacheRecord getCache(Object2ObjectLinkedOpenHashMap<ChunkPos, CacheRecord> threadLocal, ChunkPos pos) {
		final CacheRecord cached = threadLocal.getAndMoveToFirst(pos);

		// Default cache value is null
		//noinspection ConstantConditions
		if (cached != null) {
			return cached;
		}

		final CacheRecord newOne = caches.getUnchecked(pos);
		threadLocal.putAndMoveToFirst(pos, newOne);
		if (threadLocal.size() > MAX_THREAD_LOCAL_CACHE_SIZE) {
			threadLocal.removeLast();
		}

		return newOne;
	}

	BiomePerimetersImpl(Biome biome) {
		this(biome, 64);
	}

	/**
	 * new BiomePerimetersImpl()
	 * <p></p>
	 * Construct a new BiomePerimetersImpl instance for the given Biome.
	 *
	 * @param biome   Biome - The Biome for which this BiomePerimetersImpl will compute perimeter distances.
	 * @param horizon int - The maximum distance to search for perimeter points for this Biome.
	 */
	BiomePerimetersImpl(Biome biome, int horizon) {
		if (horizon < 1 || horizon > MAX_HORIZON) {
			Biolith.LOGGER.debug("BiomePerimetersImpl horizon must be in the range [1,{}]: {}", MAX_HORIZON, horizon);
			horizon = MAX_HORIZON;
		}

		this.biome = biome;
		this.cardinalHorizon = horizon;
		this.ordinalHorizon = (int) (horizon / Math.sqrt(2));
		this.checkDistance = (int) (horizon * 0.89f);
	}

	/**
	 * BiomePerimeteres.getPerimeterDistance()
	 * <p></p>
	 * Call this method when you need to know how far in-biome a block column is.  The returned int will give the
	 * distance to the perimeter if it is less than the instance's configured horizon, and a value greater than or
	 * equal to the configured horizon if it is not.
	 *
	 * @param biomeManager BiomeAccess - Biome access used to determine whether neighboring voxels are in-biome.
	 * @param pos         BlockPos - The voxel being evaluated for perimeter distance; the Y value is used for biome checks.
	 * @return int - The perimeter distance value resolved for the target voxel.
	 */
	public int getPerimeterDistance(BiomeManager biomeManager, BlockPos pos) {
		float minimum = cardinalHorizon + 1;
		BlockPos iterPos;
		int horizon;
		int dx;
		int dz;

		final var threadLocalCache = threadLocalCaches.get();

		// Work around Mojang making it illegal to access nascent chunks for biome lookup.
		Function<BlockPos, Holder<Biome>> getBiomeFunction = biomeManager::getBiome;
		if (biomeManager.noiseBiomeSource instanceof WorldGenRegion chunkRegion) {
			ServerLevel world = chunkRegion.level;
			getBiomeFunction = (blockPos) -> world.getUncachedNoiseBiome(
					QuartPos.fromBlock(blockPos.getX()),
					QuartPos.fromBlock(blockPos.getY()),
					QuartPos.fromBlock(blockPos.getZ())
			);
		}

		// If we are on the perimeter, avoid some difficult "edge" cases (har har) by short-circuiting.
		for (Direction8 direction : Direction8.values()) {
			if (!checkBiome(getBiomeFunction, pos.offset(direction.getStepX(), 0, direction.getStepZ()), threadLocalCache)) {
				return 0;
			}
		}

		// Check the cache for an authoritative distance value and return that if it's already known.
		{
			final CacheRecord cache = getCache(threadLocalCache, ChunkPos.containing(pos));
			final int idx = CacheRecord.getIndex(pos);
			if (cache.biomeCache.containsKey(idx)) {
				int cached = cache.biomeCache.get(idx);
				if (cached > 0) {
					return cached;
				}
			}
		}

		// Try to find our closest perimeter point.
		for (Direction8 direction : Direction8.values()) {
			horizon = (direction.ordinal() % 2 == 0) ? cardinalHorizon : ordinalHorizon;
			dx = direction.getStepX();
			dz = direction.getStepZ();

			for (int radius = 0; radius < horizon; radius++) {
				iterPos = pos.offset(dx * radius, 0, dz * radius);
				final CacheRecord cache = getCache(threadLocalCache, ChunkPos.containing(iterPos));
				if (cache.perimeters.containsKey(CacheRecord.getIndex(iterPos)) || !checkBiome(getBiomeFunction, iterPos.offset(dx, 0, dz), threadLocalCache)) {
					int localMinimum = this.checkPerimeter(getBiomeFunction, pos, iterPos, direction, threadLocalCache);
					if (localMinimum >= 0) {
						minimum = Math.min(minimum, localMinimum);
						break;
					} else {
						// Power on through a small biome inclusion we found.
						for (++radius; radius < horizon; radius++) {
							if (checkBiome(getBiomeFunction, pos.offset(dx * radius, 0, dz * radius), threadLocalCache)) {
								++radius;
								break;
							}
						}
					}
				}
			}
		}

		// Ensure we're about to return a sane value.  Cache the value.
		return rationalizeDistance(pos, minimum, threadLocalCache);
	}

	private int checkPerimeter(Function<BlockPos, Holder<Biome>> getBiomeFunction, BlockPos centerPos, BlockPos perimeterPos, Direction8 direction, Object2ObjectLinkedOpenHashMap<ChunkPos, CacheRecord> threadLocalCache) {
		BiomePerimeterPoint current;
		Direction8 orientation;
		double minimum;
		int localCheckDistance;

		// The point we reached the perimeter might not be known yet, so special-case it in.
		final CacheRecord perimeterPosCache = getCache(threadLocalCache, ChunkPos.containing(perimeterPos));
		final int perimeterPosIndex = CacheRecord.getIndex(perimeterPos);
		current = perimeterPosCache.perimeters.computeIfAbsent(perimeterPosIndex, k -> BiomePerimeterPoint.of(perimeterPos));

		minimum = current.getDistance(centerPos);
		localCheckDistance = Math.min(checkDistance, (int) (minimum * minimum) - 1);

		// left-hand
		orientation = direction;
		for (int check = 0; check < localCheckDistance; check++) {
			if (current.left != null) {
				orientation = getEightWayRelation(current.left.pos, current.pos);
			} else {
				orientation = getEightWayClockwiseRotation(orientation, 5);
				for (int rotation = 2; rotation < 8; rotation++) {
					orientation = getEightWayClockwiseRotation(orientation, 1);
					if (!checkBiome(getBiomeFunction, current.pos.offset(orientation.getStepX(), 0, orientation.getStepZ()), threadLocalCache)) {
						orientation = getEightWayClockwiseRotation(orientation, -1);
						BlockPos prospect = current.pos.offset(orientation.getStepX(), 0, orientation.getStepZ());
						BiomePerimeterPoint prospectPoint = getCache(threadLocalCache, ChunkPos.containing(prospect)).perimeters.get(CacheRecord.getIndex(prospect));
						//noinspection ConstantConditions
						if (prospectPoint != null) {
							prospectPoint.setRight(current);
							current.setLeft(prospectPoint);
						} else {
							current.setLeft(BiomePerimeterPoint.leftOf(prospect, current));
							getCache(threadLocalCache, ChunkPos.containing(current.left.pos)).perimeters.put(CacheRecord.getIndex(current.left.pos), current.left);
						}
						break;
					}
				}
			}

			// Special handling for inclusions of other biomes is here on the LHS.
			if (current.left == null) {
				// Dead end encountered to the left.
				break;
			} else if (perimeterPos.compareTo(current.left.pos) == 0) {
				// Handle loops caused by inclusions of other biomes.
				if (check < 16) {
					// Ignore inclusions of less than four biome pixels in size.
					return -1;
				} else {
					// Treat larger inclusions as a perimeter.
					return (int) minimum;
				}
			} else {
				// Advance.
				current = current.left;
				minimum = Math.min(minimum, current.getDistance(centerPos));
			}
		}

		// right-hand
		current = perimeterPosCache.perimeters.get(perimeterPosIndex);
		orientation = direction;
		for (int check = 0; check < localCheckDistance; check++) {
			if (current.right != null) {
				orientation = getEightWayRelation(current.right.pos, current.pos);
			} else {
				orientation = getEightWayClockwiseRotation(orientation, 3);
				for (int rotation = 2; rotation < 8; rotation++) {
					orientation = getEightWayClockwiseRotation(orientation, -1);
					if (!checkBiome(getBiomeFunction, current.pos.offset(orientation.getStepX(), 0, orientation.getStepZ()), threadLocalCache)) {
						orientation = getEightWayClockwiseRotation(orientation, 1);
						BlockPos prospect = current.pos.offset(orientation.getStepX(), 0, orientation.getStepZ());
						BiomePerimeterPoint prospectPoint = getCache(threadLocalCache, ChunkPos.containing(prospect)).perimeters.get(CacheRecord.getIndex(prospect));
						//noinspection ConstantConditions
						if (prospectPoint != null) {
							// Detect when we are passing through the same path we passed through on the left.
							if (current.equals(prospectPoint.right)) {
								// Trim single-wide channels where left == right will cause loops.
								prospectPoint.right = null;
							} else {
								prospectPoint.setLeft(current);
							}
							current.setRight(prospectPoint);
						} else {
							current.setRight(BiomePerimeterPoint.rightOf(prospect, current));
							getCache(threadLocalCache, ChunkPos.containing(current.right.pos)).perimeters.put(CacheRecord.getIndex(current.right.pos), current.right);
						}
						break;
					}
				}
			}

			// Check for loops and dead ends; advance.
			if (current.right == null || perimeterPos.compareTo(current.right.pos) == 0) {
				// Loops are already handled by the LHS.  This branch generally just handles dead ends.
				break;
			} else {
				current = current.right;
				minimum = Math.min(minimum, current.getDistance(centerPos));
			}
		}

		return (int) minimum;
	}

	private Direction8 getEightWayClockwiseRotation(Direction8 direction, int increment) {
		assert (increment >= -8);
		return Direction8.values()[(direction.ordinal() + increment + 8) % 8];
	}

	private Direction8 getEightWayRelation(BlockPos posA, BlockPos posB) {
		BlockPos diff = posA.subtract(posB);
		if (diff.getX() < 0) {
			if (diff.getZ() < 0) {
				return Direction8.NORTH_WEST;
			} else if (diff.getZ() > 0) {
				return Direction8.SOUTH_WEST;
			} else {
				return Direction8.WEST;
			}
		} else if (diff.getX() > 0) {
			if (diff.getZ() < 0) {
				return Direction8.NORTH_EAST;
			} else if (diff.getZ() > 0) {
				return Direction8.SOUTH_EAST;
			} else {
				return Direction8.EAST;
			}
		} else {
			return diff.getZ() < 0 ? Direction8.NORTH : Direction8.SOUTH;
		}
	}

	private boolean checkBiome(Function<BlockPos, Holder<Biome>> getBiomeFunction, BlockPos pos, Object2ObjectLinkedOpenHashMap<ChunkPos, CacheRecord> threadLocalCache) {
		/* Distance values in biomeCache:
		 * -1:  special value indicating pos is in-biome but the perimeter distance is unknown
		 *  0:  value indicating pos is not in-biome
		 * >0:  pos is in-biome and the value indicates the distance to the perimeter
		 */
		return (getCache(threadLocalCache, ChunkPos.containing(pos)).biomeCache.computeIfAbsent(CacheRecord.getIndex(pos), (key) -> getBiomeFunction.apply(pos).value().equals(biome) ? -1 : 0) != 0);
	}

	private int rationalizeDistance(BlockPos pos, float proposed, Object2ObjectLinkedOpenHashMap<ChunkPos, CacheRecord> threadLocalCache) {
		int distance;
		float lower = 0;
		float upper = MAX_HORIZON;

		for (Direction8 direction : Direction8.values()) {
			final BlockPos neighborPos = pos.offset(direction.getStepX(), 0, direction.getStepZ());
			int neighbor = getCache(threadLocalCache, ChunkPos.containing(neighborPos)).biomeCache.getOrDefault(CacheRecord.getIndex(neighborPos), -1);

			if (neighbor > 0) {
				// TODO: Leaving the lower bound out gives better results for Calderas ... could use some thought.
				//lower = Math.max(lower, neighbor - 2.72f);
				upper = Math.min(upper, neighbor + 2.72f);
			}
		}

		distance = Math.round(Mth.clamp(proposed, lower, upper));
		getCache(threadLocalCache, ChunkPos.containing(pos)).biomeCache.put(CacheRecord.getIndex(pos), distance);

		return distance;
	}

	private static Function<BlockPos, Holder<Biome>> selectGetBiomeFunction(BiomeManager biomeAccess) {
		/*
		 * When the BiomeAccess uses a ChunkRegion, it will deny lookup rather than creating a Chunk.
		 * This implementation bypasses the BiomeAccess to use direct biome lookups.
		 * We are forced to reimplement the getBiome smoothing function.
		 */
		if (biomeAccess.noiseBiomeSource instanceof WorldGenRegion chunkRegion) {
			ServerLevel world = chunkRegion.level;

			return (pos) -> VanillaCompat.callFunctionWithSmoothedBiomeCoords(world::getUncachedNoiseBiome, pos, world.getSeed());
		}

		// Fall back to the vanilla getBiome, which may work with some other biome access implementations...
		return biomeAccess::getBiome;
	}

	/**
	 * BiomePerimetersImpl.getOrCreateInstance()
	 * <p></p>
	 * Each Biome must have a separate instance of BiomePerimetersImpl.  An instance of BiomePerimetersImpl
	 * will be created if one does not already exist, and subsequently the same instance will always
	 * be returned for the given Biome during the same game session.
	 *
	 * @param biome   - The Biome for which we are maybe-adding and fetching the BiomePerimetersImpl instance
	 * @param horizon - Max distance to check for biome edge; range [1 - 256]
	 */
	public static synchronized BiomePerimetersImpl getOrCreateInstance(Biome biome, int horizon) {
		return instances.computeIfAbsent(biome, (key) -> new BiomePerimetersImpl(key, horizon));
	}


	/**
	 * BiomePerimetersImpl.BiomePerimeterPoint
	 * <p></p>
	 * This class essentially defines a mutable record we use to store data about the perimeter of a Biome.
	 * A future implementation of BiomePerimetersImpl may expose BiomePerimeterPoints in some manner if useful.
	 */
	public static final class BiomePerimeterPoint implements Cloneable {
		final BlockPos pos;
		@Nullable BiomePerimeterPoint left;
		@Nullable BiomePerimeterPoint right;

		public BiomePerimeterPoint(BlockPos pos, @Nullable BiomePerimeterPoint left, @Nullable BiomePerimeterPoint right) {
			this.pos = pos.immutable();
			this.left = left;
			this.right = right;
		}

		@Override
		public Object clone() throws CloneNotSupportedException {
			return super.clone();
		}

		public static BiomePerimeterPoint of(BlockPos pos) {
			return new BiomePerimeterPoint(pos, null, null);
		}

		public static BiomePerimeterPoint rightOf(BlockPos pos, BiomePerimeterPoint left) {
			return new BiomePerimeterPoint(pos, left, null);
		}

		public static BiomePerimeterPoint leftOf(BlockPos pos, BiomePerimeterPoint right) {
			return new BiomePerimeterPoint(pos, null, right);
		}

		public static BiomePerimeterPoint of(BlockPos pos, BiomePerimeterPoint left, BiomePerimeterPoint right) {
			return new BiomePerimeterPoint(pos, left, right);
		}

		public void setLeft(BiomePerimeterPoint left) {
			assert (this.left == null);
			this.left = left;
		}

		public void setRight(BiomePerimeterPoint right) {
			assert (this.right == null);
			this.right = right;
		}

		public BlockPos getPos() {
			return pos.mutable();
		}

		public @Nullable BiomePerimeterPoint getLeft() {
			if (left != null) {
				try {
					return (BiomePerimeterPoint) left.clone();
				} catch (CloneNotSupportedException ignored) {}
			}

				return null;
		}

		public @Nullable BiomePerimeterPoint getRight() {
			if (right != null) {
				try {
					return (BiomePerimeterPoint) right.clone();
				} catch (CloneNotSupportedException ignored) {}
			}

			return null;
		}

		/**
		 * BiomePerimetersImpl.BiomePerimeterPoint.getDistance()
		 * <p></p>
		 * Calculate the straight-line distance between a biome perimeter point and another position.
		 *
		 * @param pos BlockPos - The position to calculate the distance to
		 * @return double - The distance "as the crow flies" between this BiomePerimeterPoint and pos
		 */
		public double getDistance(BlockPos pos) {
			return Math.sqrt(this.pos.distSqr(pos));
		}

		/**
		 * BiomePerimetersImpl.BiomePerimeterPoint.getTaxicab()
		 * <p></p>
		 * Calculate the taxi-cab (or "Manhattan") distance between a biome perimeter point and another position.
		 * This is the sum of the difference between the X and Z coordinates of the two points.
		 *
		 * @param pos Vec3i - The position to calculate the taxi-cab distance to
		 * @return int - The distance "as a taxi drives" between this BiomePerimeterPoint and pos
		 */
		public int getTaxicab(Vec3i pos) {
			return this.pos.distManhattan(pos);
		}
	}


	private static final class CacheRecord {
		private final Int2ReferenceMap<BiomePerimeterPoint> perimeters = Int2ReferenceMaps.synchronize(new Int2ReferenceOpenHashMap<>(16 * 16 * 4), this);
		private final Int2IntMap biomeCache = Int2IntMaps.synchronize(new Int2IntOpenHashMap(16 * 16 * 4), this);

		public void clear() {
			this.perimeters.clear();
			this.biomeCache.clear();
		}

		public static int getIndex(int x, int y, int z) {
			return (x & 0b1111) | ((z & 0b1111) << 4) | (((short) y) << 8);
		}

		public static int getIndex(BlockPos pos) {
			return getIndex(pos.getX(), pos.getY(), pos.getZ());
		}
	}
}
