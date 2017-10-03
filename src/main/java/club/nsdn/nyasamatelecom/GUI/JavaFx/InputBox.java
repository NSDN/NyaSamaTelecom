package club.nsdn.nyasamatelecom.GUI.JavaFx;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

import java.util.LinkedList;

/**
 * Created by drzzm on 2017.3.9.
 */

public class InputBox extends Application {

    public static Thread theThread;
    protected static LinkedList<Runnable> threadQueue;
    protected static InputBox instance;
    protected boolean isInit = false;

    public static Stage stage;
    public static GridPane root;
    public static ICallback callback;

    static final String title = "NSASM Editor";
    static final int width = 640;
    static final int height = 360;

    public static InputBox getInstance() {
        if (instance == null) instance = new InputBox();
        return instance;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        //Platform.setImplicitExit(false);
        stage = primaryStage;
        stage.setMinWidth(640);
        stage.setMinHeight(360);
        stage.setOnCloseRequest(event -> {
            //event.consume();
            stage.close();
        });

        root = new GridPane();
        root.setAlignment(Pos.CENTER);
        root.setHgap(10); root.setVgap(10);
        root.setMinSize(640, 360);
        root.setPadding(new Insets(5.0, 5.0, 5.0, 5.0));

        TextArea textArea = new TextArea();
        GridPane.setHalignment(textArea, HPos.LEFT);
        GridPane.setValignment(textArea, VPos.TOP);
        GridPane.setHgrow(textArea, Priority.ALWAYS);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setMargin(textArea, new Insets(5.0, 5.0, 0.0, 5.0));
        textArea.setId("content");
        root.add(textArea, 0, 0);

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10); gridPane.setVgap(10);
        GridPane.setHalignment(textArea, HPos.LEFT);
        GridPane.setValignment(textArea, VPos.BOTTOM);
        GridPane.setHgrow(textArea, Priority.ALWAYS);
        GridPane.setMargin(gridPane, new Insets(0.0, 5.0, 5.0, 5.0));
        gridPane.setAlignment(Pos.BASELINE_RIGHT);
        gridPane.setHgap(10); gridPane.setVgap(10);

        Button btnOK = new Button("OK");
        btnOK.setOnMouseClicked(event -> {
            stage.hide();
            callback.run(((TextArea) root.lookup("#content")).getText());
            ((TextArea) root.lookup("#content")).clear();
        });
        gridPane.add(btnOK, 0, 0);
        Button btnCancel = new Button("Cancel");
        btnCancel.setCancelButton(true);
        btnCancel.setOnMouseClicked(event -> {
            stage.hide();
            callback.run("");
            ((TextArea) root.lookup("#content")).clear();
        });
        gridPane.add(btnCancel, 1, 0);

        root.add(gridPane, 0, 1);

        stage.setTitle(title);
        stage.setScene(new Scene(root, width, height));
        stage.show();
    }

    public void show(ICallback callback) {
        InputBox.callback = callback;
        if (!isInit) {
            isInit = true;
            threadQueue = new LinkedList<>();
            theThread = new Thread(() -> {
                while (true) {
                    if (!threadQueue.isEmpty()) {
                        threadQueue.poll().run();
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        break;
                    }
                    System.out.println("TICK!");
                }
            });
            theThread.setName("JavaFX thread");
            threadQueue.offer(() -> launch());
            theThread.start();
        } else {
            threadQueue.clear();
            threadQueue.offer(() -> {
                stage.show();
                System.out.println("SHOW!");
            });
        }
    }

    public void hide() {
        threadQueue.clear();
        threadQueue.offer(() -> {
            stage.close();
            System.out.println("HIDE!");
        });
    }

}
