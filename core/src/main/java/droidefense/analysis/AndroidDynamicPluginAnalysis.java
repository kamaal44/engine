package droidefense.analysis;

import apkr.external.modules.helpers.log4j.Log;
import apkr.external.modules.helpers.log4j.LoggerType;
import droidefense.analysis.base.AbstractAndroidAnalysis;
import droidefense.sdk.AbstractDynamicPlugin;
import droidefense.sdk.helpers.InternalConstant;
import droidefense.sdk.model.base.DroidefenseProject;
import droidefense.sdk.model.base.ExecutionTimer;
import droidefense.sdk.model.io.AbstractHashedFile;
import droidefense.worker.handler.DirScannerHandler;
import droidefense.worker.handler.FileIOHandler;
import droidefense.worker.handler.base.DirScannerFilter;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by sergio on 17/2/16.
 */
public final class AndroidDynamicPluginAnalysis extends AbstractAndroidAnalysis {

    public transient static final String PLUGIN_PACKAGE_NAME = "droidefense.droidefense.om.plugins.collection.dynamicscan.";

    public AndroidDynamicPluginAnalysis() {
        timeStamp = new ExecutionTimer();
    }

    @Override
    public boolean analyze() {
        executionSuccessful = false;
        //set current currentProject
        currentProject = DroidefenseProject.getProject(apkFile);
        Log.write(LoggerType.TRACE, "\n\n --- Running Droidefense dynamic plugin analysis ---\n\n");
        File plFolder = FileIOHandler.getDynamicPluginsFolderFile();
        if (plFolder.exists()) {
            DirScannerHandler scanner = new DirScannerHandler(plFolder, false, new DirScannerFilter() {
                @Override
                public boolean addFile(File f) {
                    return f.getName().endsWith(InternalConstant.COMPILED_JAVA_EXTENSION);
                }
            });
            scanner.doTheJob();
            ArrayList<AbstractHashedFile> pluginsList = scanner.getFiles();
            for (AbstractHashedFile plugin : pluginsList) {
                //run each plugin in a different thread
                String pluginName = plugin.getName();
                if (pluginName.endsWith(InternalConstant.COMPILED_JAVA_EXTENSION)) {
                    Log.write(LoggerType.TRACE, plugin.getAbsolutePath());
                    Class aClass = null;
                    try {
                        ClassLoader classLoader = this.getClass().getClassLoader();
                        aClass = classLoader.loadClass(PLUGIN_PACKAGE_NAME + pluginName.replace(".class", ""));
                        Log.write(LoggerType.TRACE, "Executing " + aClass.getName());
                        AbstractDynamicPlugin pluginDynamic = (AbstractDynamicPlugin) aClass.newInstance();

                        pluginDynamic.setApk(apkFile);
                        pluginDynamic.setCurrentProject(currentProject);
                        //TODO fix this name
                        //name = pluginDynamic.getPluginName();
                        pluginDynamic.analyze();
                        //result = pluginDynamic.getResult();

                        //add result to currentProject
                        currentProject.addDynamicPlugin(pluginDynamic);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                        addError(e);
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                        addError(e);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                        addError(e);
                    } catch (Exception e) {
                        e.printStackTrace();
                        addError(e);
                    }
                } else {
                    //todo load jar content using reflection
                    Log.write(LoggerType.TRACE, plugin.getAbsolutePath());
                }
            }
        }
        Log.write(LoggerType.TRACE, "\n--- RUNNING PLUGINS (DONE)---\n");
        //stop timer
        stop();
        executionSuccessful = true;
        return executionSuccessful;
    }

    @Override
    public String getName() {
        return "Android dynamic plugin analysis";
    }

}
