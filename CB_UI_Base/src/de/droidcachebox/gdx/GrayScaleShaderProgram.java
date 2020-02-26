package de.droidcachebox.gdx;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public class GrayScaleShaderProgram extends ShaderProgram {

    final static String NAME = "GrayScaleShader";

    final static String VERT = "attribute vec4 " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" + "attribute vec4 " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" + "attribute vec2 " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" +

            "uniform mat4 u_projTrans;\n" + " \n" + "varying vec4 vColor;\n" + "varying vec2 vTexCoord;\n" +

            "void main() {\n" + "       vColor = " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" + "       vTexCoord = " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" + "       gl_Position =  u_projTrans * " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" + "}";

    final static String FRAG =
            // GL ES specific stuff
            "#ifdef GL_ES\n" //
                    + "#define LOWP lowp\n" //
                    + "precision mediump float;\n" //
                    + "#else\n" //
                    + "#define LOWP \n" //
                    + "#endif\n" + //
                    "varying LOWP vec4 vColor;\n" + "varying vec2 vTexCoord;\n" + "uniform sampler2D u_texture;\n" + "uniform float grayscale;\n" + "void main() {\n" + "       vec4 texColor = texture2D(u_texture, vTexCoord);\n" + "       \n"
                    + "       float gray = dot(texColor.rgb, vec3(0.299, 0.587, 0.114));\n" + "       texColor.rgb = mix(vec3(gray), texColor.rgb, grayscale);\n" + "       \n" + "       gl_FragColor = texColor * vColor;\n" + "}";

    static {
        ShaderProgram.pedantic = false;
    }

    public GrayScaleShaderProgram() {
        super(VERT, FRAG);

        // shader didn't compile.. handle it somehow
        if (!this.isCompiled()) {
            System.err.println(this.getLog());
            System.exit(0);
        }

        // incase there were any warnings/info
        if (this.getLog().length() != 0)
            System.out.println(this.getLog());
    }

    @Override
    public String toString() {
        return NAME;
    }

}
