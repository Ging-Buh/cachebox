import com.badlogic.gdx.jnigen.AntScriptGenerator;
import com.badlogic.gdx.jnigen.BuildConfig;
import com.badlogic.gdx.jnigen.BuildExecutor;
import com.badlogic.gdx.jnigen.BuildTarget;
import com.badlogic.gdx.jnigen.BuildTarget.TargetOs;
import com.badlogic.gdx.jnigen.NativeCodeGenerator;


public class CB_Native_Build {

	public static final String br = System.getProperty("line.separator");
	
	public static void main(String[] args) throws Exception
	{
		System.out.print("Begin CB_Native build"+br+br);
		System.out.print("Create Native Code"+br);
		new NativeCodeGenerator().generate("src", "bin", "jni");

		BuildTarget win32home = BuildTarget.newDefaultTarget(TargetOs.Windows, false);
        win32home.compilerPrefix = "";
        win32home.buildFileName = "build-windows32home.xml";
        win32home.excludeFromMasterBuildFile = true;
        win32home.cFlags += " -DHAVE_CONFIG_H";

        BuildTarget win32 = BuildTarget.newDefaultTarget(TargetOs.Windows, false);
        win32.cFlags += " -DHAVE_CONFIG_H";

        BuildTarget win64 = BuildTarget.newDefaultTarget(TargetOs.Windows, true);
        win64.cFlags += " -DHAVE_CONFIG_H";

        BuildTarget lin32 = BuildTarget.newDefaultTarget(TargetOs.Linux, false);
        lin32.cFlags += " -DHAVE_CONFIG_H";

        BuildTarget lin64 = BuildTarget.newDefaultTarget(TargetOs.Linux, true);
        lin64.cFlags += " -DHAVE_CONFIG_H";

        BuildTarget mac = BuildTarget.newDefaultTarget(TargetOs.MacOsX, false);
        mac.cFlags += " -DHAVE_CONFIG_H";

        BuildTarget android = BuildTarget.newDefaultTarget(TargetOs.Android, false);
        android.cFlags += " -DHAVE_CONFIG_H";

        new AntScriptGenerator().generate(new BuildConfig("CB"),   win64);

        
        BuildExecutor.executeAnt("jni\\build.xml", "-v");
 	}
	
	
	
}
