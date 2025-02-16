package info.megadrum.managerfx.data;

import java.io.*;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

//import javax.naming.spi.DirStateFactory.Result;

//import org.apache.commons.configuration.CombinedConfiguration;
//import org.apache.commons.configuration.ConfigurationException;
//import org.apache.commons.configuration.ConversionException;
//import org.apache.commons.configuration.PropertiesConfiguration;
//import org.apache.commons.configuration.PropertiesConfigurationLayout;

import info.megadrum.managerfx.utils.Constants;
import info.megadrum.managerfx.utils.Utils;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.PropertiesConfigurationLayout;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.ex.ConversionException;

public class FileManager {
    private FileChooser fileChooser;
    private Stage parent;
    private File file = null;
    //private ConfigFileFilter configFileFilter;
    //private BinFileFilter binFileFilter;
    //private Properties prop;
    private PropertiesConfiguration fullConfig;
    //private CombinedConfiguration cc;

    public FileManager (Stage parentWindow) {
        fileChooser = new FileChooser();
        parent = parentWindow;
        //binFileFilter = new BinFileFilter();
        //prop = new Properties();
        //fullConfig = new PropertiesConfiguration();
    }

    public void saveConfigFull(ConfigFull config, File file) {
        if (file.exists() && !file.delete()) {
            Utils.show_error("Error when deleting the file: " + file.getAbsolutePath());
            return;
        }

        PropertiesConfiguration fullConfig = new PropertiesConfiguration();
        PropertiesConfigurationLayout layout = new PropertiesConfigurationLayout();
        layout.setHeaderComment("MegaDrum config");

        try {
            config.copyToPropertiesConfiguration(fullConfig, layout);

            try (FileWriter writer = new FileWriter(file)) {
                layout.save(fullConfig, writer);
            }
        } catch (ConfigurationException | IOException e) {
            Utils.show_error("Error when saving the settings in:\n" +
                    file.getAbsolutePath() + "\n(" + e.getMessage() + ")");
        }
    }

    public boolean save_all(ConfigFull config, ConfigOptions options) {
        boolean result = false;
        FileChooser.ExtensionFilter configFileFilter = new FileChooser.ExtensionFilter("MegaDrum full config files (*.mds)", "*.mds");
        fileChooser.getExtensionFilters().clear();
        fileChooser.getExtensionFilters().add(configFileFilter);
        if (!options.configFullPaths[options.lastConfig].equals("")) {
            fileChooser.setInitialDirectory(new File(options.configFullPaths[options.lastConfig]).getParentFile());
        }
        //fileChooser.setSelectedFile(new File(options.configFileNames[options.lastConfig]));
        fileChooser.setInitialFileName(options.configFileNames[options.lastConfig]);
        //returnVal = fileChooser.showSaveDialog(parent);
        file = fileChooser.showSaveDialog(parent);
        if(file != null) {
            result = true;
            if (!(file.getName().toLowerCase().endsWith(".mds"))) {
                file = new File(file.getAbsolutePath() + ".mds");
            }
            options.configFullPaths[options.lastConfig] = file.getAbsolutePath();
            options.configFileNames[options.lastConfig] = file.getName();
            options.configLoaded[options.lastConfig] = true;

            if (file.exists()) {
                file.delete();
            }
            saveConfigFull(config, file);
        }
        return result;
    }

    public void loadConfigFull(ConfigFull config, File file, ConfigOptions options) {
        fullConfig = new PropertiesConfiguration();
        PropertiesConfigurationLayout layout = new PropertiesConfigurationLayout();

        try (FileReader reader = new FileReader(file)) {
            layout.load(fullConfig, reader);

            try {
                config.copyFromPropertiesConfiguration(fullConfig);
                options.configLoaded[options.lastConfig] = true;
            } catch (ConversionException e) {
                Utils.show_error("Error when parsing the settings:\n" +
                        file.getAbsolutePath() + "\n(" + e.getMessage() + ")");
            }
        } catch (ConfigurationException | IOException e) {
            Utils.show_error("Error when loading the settings:\n" +
                    file.getAbsolutePath() + "\n(" + e.getMessage() + ")");
        }
    }

    public boolean load_all(ConfigFull config, ConfigOptions options) {
        boolean result = false;
        FileChooser.ExtensionFilter configFileFilter = new FileChooser.ExtensionFilter("MegaDrum full config files (*.mds)", "*.mds");
        fileChooser.getExtensionFilters().clear();
        fileChooser.getExtensionFilters().add(configFileFilter);
        if (!options.configFullPaths[options.lastConfig].equals("")) {
            fileChooser.setInitialDirectory(new File(options.configFullPaths[options.lastConfig]).getParentFile());
        }
        //fileChooser.setSelectedFile(new File(options.configFileNames[options.lastConfig]));
        fileChooser.setInitialFileName(options.configFileNames[options.lastConfig]);
        //returnVal = fileChooser.showSaveDialog(parent);
        file = fileChooser.showOpenDialog(parent);
        if (file != null) {
            if (file.exists()) {
                result = true;
                loadConfigFull(config,file,options);
                options.configFullPaths[options.lastConfig] = file.getAbsolutePath();
                options.configFileNames[options.lastConfig] = file.getName();
            }
        }
        return result;
    }


    public File selectFirmwareFile(ConfigOptions options) {
        FileChooser.ExtensionFilter extensionFilter;
        String stringNameMatch;
        if (!options.lastFullPathFirmware.equals("")) {
            fileChooser.setInitialDirectory(new File(options.lastFullPathFirmware).getParentFile());
        }
        extensionFilter = new FileChooser.ExtensionFilter("Firmware files (*.bin)", "*.bin");
        stringNameMatch = "";
        fileChooser.getExtensionFilters().clear();
        fileChooser.getExtensionFilters().add(extensionFilter);
        if (options.mcuType == 4) {
            stringNameMatch = "megadrumSTM32a_";
        }
        if (options.mcuType == 5) {
            stringNameMatch = "megadrumSTM32b_";
        }
        if (options.mcuType == 6) {
            stringNameMatch = "megadrumSTM32c_";
        }
        if (options.mcuType == 7) {
            stringNameMatch = "megadrumSTM32d_";
        }
        if (options.mcuType == 8) {
            stringNameMatch = "megadrumSTM32e_";
        }
        //fileChooser
        file = fileChooser.showOpenDialog(parent);
        if (file != null) {
            options.lastFullPathFirmware = file.getAbsolutePath();
            String stringNameSelected = file.getName().toLowerCase();
            if (stringNameSelected.indexOf(stringNameMatch.toLowerCase()) != 0) {
                file = null;
                Alert alert = new Alert(AlertType.WARNING);
                alert.setHeaderText("Wrong firmware file selected!\n" +
                        "File name must start with " + stringNameMatch + " !!!"
                );
                alert.showAndWait();
                //WebView webView = new WebView();
                //webView.getEngine().loadContent("<html><font size=4>Wrong firmware file selected<br></font></html>\n" +
                //		"<html><font size=4>File name must start with " + stringNameMatch +" !!!</font></html>");
                //webView.setPrefSize(300, 120);
                //alert.getDialogPane().setContent(webView);
	            /*
				Timer warning_timer = new Timer();
				warning_timer.schedule(new TimerTask() {

					@Override
					public void run() {
						Platform.runLater(new Runnable() {
							public void run() {
								alert.showAndWait();
							}
						});
					}
				}, 50);
				*/
            }
        }
        return file;
    }

    public void loadAllSilent(ConfigFull config, ConfigOptions options) {
        file = new File(options.configFullPaths[options.lastConfig]);
        if (file.exists()) {
            if (!file.isDirectory()) {
                loadConfigFull(config,file,options);
                options.configFileNames[options.lastConfig] = file.getName();
                options.configFullPaths[options.lastConfig] = file.getAbsolutePath();
            }
        }
    }

    public ConfigOptions loadLastOptions(ConfigOptions config) {
        File file = new File(Constants.MD_MANAGER_CONFIG);

        if (file.exists()) {
            PropertiesConfiguration prop = new PropertiesConfiguration();
            PropertiesConfigurationLayout layout = new PropertiesConfigurationLayout();

            try (FileReader reader = new FileReader(file)) {
                layout.load(prop, reader);

                try {
                    config.copyFromPropertiesConfiguration(prop);
                } catch (ConversionException e) {
                    Utils.show_error("Error parsing the MegaDrum options from the file:\n" +
                            file.getAbsolutePath() + "\n(" + e.getMessage() + ")");
                }
            } catch (ConfigurationException | IOException e) {
                saveLastOptions(config);
            }
        } else {
            saveLastOptions(config);
        }
        return config;
    }

    public void saveLastOptions(ConfigOptions config) {
        File file = new File(Constants.MD_MANAGER_CONFIG);
        PropertiesConfiguration prop = new PropertiesConfiguration();
        PropertiesConfigurationLayout layout = new PropertiesConfigurationLayout();

        layout.setHeaderComment("MegaDrum Options Configuration");

        config.copyToPropertiesConfiguration(prop);

        try (FileWriter writer = new FileWriter(file)) {
            layout.save(prop, writer);
        } catch (ConfigurationException | IOException e) {
            Utils.show_error("Error when saving the MegaDrum options in:\n" +
                    file.getAbsolutePath() + "\n(" + e.getMessage() + ")");
        }
    }


    public void saveSysex(byte [] sysex, ConfigOptions options) {
        FileChooser.ExtensionFilter sysexFileFilter = new FileChooser.ExtensionFilter("Sysex files (*.syx)", "*.syx");
        fileChooser.getExtensionFilters().clear();
        fileChooser.getExtensionFilters().add(sysexFileFilter);

        if (!options.lastFullPathSysex.equals("")) {
            fileChooser.setInitialDirectory(new File(options.lastFullPathSysex).getParentFile());
        }
        file = fileChooser.showSaveDialog(parent);
        if (file != null) {
            if (!(file.getName().toLowerCase().endsWith(".syx"))) {
                file = new File(file.getAbsolutePath() + ".syx");
            }
            options.lastFullPathSysex = file.getAbsolutePath();
            if (file.exists()) {
                file.delete();
            }
            try {
                file.createNewFile();
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(sysex);
                fos.flush();
                fos.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                //e.printStackTrace();
                Utils.show_error("Error saving Sysex to file:\n" +
                        file.getAbsolutePath() + "\n"
                        +"(" + e.getMessage() + ")");

            }
        }
    }

    public void loadSysex(byte [] sysex, ConfigOptions options) {
        FileChooser.ExtensionFilter sysexFileFilter = new FileChooser.ExtensionFilter("Sysex files (*.syx)", "*.syx");
        fileChooser.getExtensionFilters().clear();
        fileChooser.getExtensionFilters().add(sysexFileFilter);

        if (!options.lastFullPathSysex.equals("")) {
            fileChooser.setInitialDirectory(new File(options.lastFullPathSysex).getParentFile());
        }
        file = fileChooser.showOpenDialog(parent);
        if (file != null) {
            options.lastFullPathSysex = file.getAbsolutePath();
            if (file.exists()) {
                FileInputStream fis;
                try {
                    fis = new FileInputStream(file);
                    fis.read(sysex);
                    fis.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    // e.printStackTrace();
                    Utils.show_error("Error loading Sysex from file:\n" +
                            file.getAbsolutePath() + "\n"
                            +"(" + e.getMessage() + ")");
                }
            }
        }
    }
}