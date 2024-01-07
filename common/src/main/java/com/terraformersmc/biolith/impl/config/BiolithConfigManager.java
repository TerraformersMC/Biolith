package com.terraformersmc.biolith.impl.config;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.terraformersmc.biolith.impl.Biolith;
import com.terraformersmc.biolith.impl.platform.Services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class BiolithConfigManager {
	private final Path generalConfigPath;
	private final Path generalConfigBackupPath;
	private BiolithGeneralConfig generalConfig;

	private static final Gson GSON = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).setPrettyPrinting().create();

	public BiolithConfigManager() {
		Path configDirectory = Services.PLATFORM.getConfigDir().resolve(Biolith.MOD_ID);

		try {
			Files.createDirectories(configDirectory);
		} catch (IOException e) {
			Biolith.LOGGER.error("Failed to create config directory at " + configDirectory, e);
		}

		generalConfigPath = configDirectory.resolve("general.json");
		generalConfigBackupPath = configDirectory.resolve("general-invalid-syntax.json");
	}

	private static <T> T loadConfig(Path configPath, T defaults, Class<T> clazz, Path backupPath) {
		if (!Files.exists(configPath)) {
			if (!saveConfig(configPath, defaults)) {
				Biolith.LOGGER.error("Unable to save default configuration values for " + configPath);

				// Not much else to do at this point.
				return defaults;
			}
		}

		String content;

		try {
			// Load the entire file first, so that we don't get any weird IO errors halfway through.
			content = Files.readString(configPath);
		} catch (IOException e) {
			Biolith.LOGGER.error("Failed to load Biolith configuration file at " + configPath, e);
			Biolith.LOGGER.error("This shouldn't happen under normal conditions, ensure that you have the correct permissions");
			Biolith.LOGGER.error("Reverting to default configuration");

			return defaults;
		}

		try {
			return GSON.fromJson(content, clazz);
		} catch (JsonSyntaxException e) {
			Biolith.LOGGER.error("Failed to parse Biolith configuration file at " + configPath, e);

			// Revert the config so that the user has a fresh start if they need it.
			// It's also possible for the user to delete the config to recreate it, but that seems a bit unintuitive.
			Biolith.LOGGER.error("Reverting to default configuration, ensure that your file has correct syntax");
			saveConfig(configPath, defaults);

			// There are a few websites like this, but this was the first result that came up.
			Biolith.LOGGER.error("In the future, consider using something like https://jsonchecker.com/ to check your syntax");

			// It would be quite annoying if a user just spent 10 minutes editing the file, only for it to be wiped away.
			// Save a backup so they can review the errors and fix them.
			Biolith.LOGGER.error("The previous configuration file content has been written to " + backupPath);

			try {
				Files.writeString(backupPath, content);
			} catch (IOException ioe) {
				Biolith.LOGGER.error("Couldn't save previous configuration file content at " + backupPath, ioe);
				Biolith.LOGGER.error("This shouldn't happen under normal conditions, ensure that you have the correct permissions and that your disk isn't full!");
			}

			return defaults;
		}
	}

	private static <T> boolean saveConfig(Path configPath, T instance) {
		String jsonString = GSON.toJson(instance);

		try {
			Files.writeString(configPath, jsonString);

			return true;
		} catch (IOException e) {
			Biolith.LOGGER.error("Couldn't save Biolith configuration file at " + configPath, e);
			Biolith.LOGGER.error("This shouldn't happen under normal conditions, ensure that you have the correct permissions and that your disk isn't full!");

			return false;
		}
	}

	public BiolithGeneralConfig getGeneralConfig() {
		if (generalConfig == null) {
			generalConfig = loadConfig(generalConfigPath, new BiolithGeneralConfig(), BiolithGeneralConfig.class, generalConfigBackupPath);
		}

		return generalConfig;
	}
}
