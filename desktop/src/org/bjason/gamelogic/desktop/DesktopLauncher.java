package org.bjason.gamelogic.desktop;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import org.bjason.gamelogic.FirstScreen;
import org.bjason.gamelogic.GameProxy$;
import org.bjason.gamelogic.GameSetup$;
import org.bjason.gamelogic.*;

import java.io.*;
import java.util.Scanner;


public class DesktopLauncher {
    public static void main(String[] arg) throws IOException {

        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();

        /*
        String home = System.getProperty("user.home");

        File file = new File(home+"/"+"._playerid.txt");
        if (file.exists()) {

            Scanner s = new Scanner(file);
            String content = s.next();
            s.close();
            System.out.println(content);

            int playerNumber = Integer.parseInt(content);

            GameSetup$.MODULE$._playerId_$eq(playerNumber);

            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write("" + (playerNumber + 1));
            writer.close();

            if (playerNumber % 2 == 0) {
                config.x = 0;
            } else {
                config.x = 1200;
            }
            config.y = 0;
        }


        */
        config.width = 1024;
        config.height = 700;

        config.fullscreen = true;
        GameProxy$ game = GameProxy$.MODULE$;
        game.setAdapter(new FirstScreen());
        LwjglApplication l = new LwjglApplication(game, config);
        Gdx.app.setLogLevel(Application.LOG_INFO);

    }
}
