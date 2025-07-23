package de.redstonecloud.cloud.plugin;

import com.google.common.base.Preconditions;
import de.redstonecloud.cloud.RedstoneCloud;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@Log4j2
public abstract class Plugin {
    protected boolean enabled = false;
    private PluginData description;
    private RedstoneCloud cloud;
    private File pluginFile;
    private File dataFolder;
    private boolean initialized = false;

    public Plugin() {
    }

    protected final void init(PluginData description, RedstoneCloud cloud, File pluginFile) {
        Preconditions.checkArgument(!this.initialized, "Plugin has been already initialized!");
        this.initialized = true;
        this.description = description;
        this.cloud = cloud;

        this.pluginFile = pluginFile;
        this.dataFolder = new File(RedstoneCloud.getWorkingDir() + "/plugins/" + description.getName().toLowerCase() + "/");
        if (!this.dataFolder.exists()) {
            this.dataFolder.mkdirs();
        }
    }

    public void onLoad() {
    }

    public abstract void onEnable();

    public void onDisable() {
    }

    public InputStream getResourceFile(String filename) {
        try {
            JarFile pluginJar = new JarFile(this.pluginFile);
            JarEntry entry = pluginJar.getJarEntry(filename);
            return pluginJar.getInputStream(entry);
        } catch (IOException e) {
        }
        return null;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled) {
            return;
        }
        this.enabled = enabled;
        try {
            if (enabled) {
                this.onEnable();
            } else {
                this.onDisable();
            }
        } catch (Exception e) {
            log.error("Error while enabling/disabling plugin " + this.getName() + ": " + e);
        }
    }

    public PluginData getDescription() {
        return this.description;
    }

    public String getName() {
        return this.description.getName();
    }

    public RedstoneCloud getCloud() {
        return this.cloud;
    }

    public Logger getLogger() {
        return log;
    }

    public File getDataFolder() {
        return this.dataFolder;
    }
}