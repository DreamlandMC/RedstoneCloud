package de.redstonecloud.cloud.commands.defaults;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.redstonecloud.api.util.EmptyArrays;
import de.redstonecloud.cloud.commands.Command;
import de.redstonecloud.cloud.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;

@Slf4j
public class UpdateCommand extends Command {

    public UpdateCommand(String cmd) {
        super(cmd);
    }

    @Override
    protected void onCommand(String[] args) {
        if (args.length < 1) {
            log.warn("Usage: update <templateName> [--reboot]");
            return;
        }

        String templateName = args[0];
        boolean reboot = args.length > 1 && args[1].equalsIgnoreCase("--reboot");

        try {
            File cfgFile = new File("./template_configs/" + templateName + ".json");
            if (!cfgFile.exists()) {
                log.error("template_cfg.json for template '{}' not found.", templateName);
                return;
            }

            String json = FileUtils.readFileToString(cfgFile, StandardCharsets.UTF_8);
            JsonObject cfg = JsonParser.parseString(json).getAsJsonObject();

            String type = cfg.get("type").getAsString().toUpperCase();

            String jarName = isProxyType(type) ? "proxy.jar" : "server.jar";

            Utils.updateSoftware(templateName, type, jarName, reboot);

        } catch (Exception e) {
            log.error("Failed to update template '{}'", args[0], e);
        }
    }

    private boolean isProxyType(String type) {
        return type.equalsIgnoreCase("WDPE");
    }

    @Override
    public String[] getArgs() {
        return getServer().getServerManager().getTemplates().keySet().toArray(EmptyArrays.STRING);
    }
}
