package club.nsdn.nyasamatelecom.api.tool.util;

import club.nsdn.nyasamatelecom.api.network.NGTPacket;
import cn.ac.nya.nsasm.NSASM;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * Created by drzzm32 on 2020.3.31.
 */
public class NGTEditor extends JFrame {

    private static final String CMD_OK = "OK";

    private JPanel panel;
    private JTextArea area;
    private JButton btn;

    private SimpleNetworkWrapper wrapper;

    public NGTEditor(SimpleNetworkWrapper wrapper) {
        super("NGT Editor" + " v" + NSASM.version);
        this.wrapper = wrapper;

        init();
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.setSize(360,640);
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        this.setVisible(true);
    }

    public void setCode(String code) {
        area.setText(code);
    }

    private Font getFont(String name, float size) {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        for (Font f : ge.getAllFonts())
            if (f.getName().equalsIgnoreCase(name))
                return f.deriveFont(size);
        for (Font f : ge.getAllFonts())
            if (f.getName().equalsIgnoreCase("Arial"))
                return f.deriveFont(size);
        return null;
    }

    private void init() {
        panel = new JPanel();
        panel.setLayout(new BorderLayout(4, 4));
        panel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        area = new JTextArea();
        area.setFont(this.getFont("Courier New", 14.0F));
        area.setWrapStyleWord(true);
        area.setLineWrap(true);
        area.setTabSize(4);
        JScrollPane pane = new JScrollPane(area,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        pane.setAutoscrolls(true);
        panel.add(pane, BorderLayout.CENTER);
        btn = new JButton("OK");
        btn.setActionCommand(CMD_OK);
        panel.add(btn, BorderLayout.SOUTH);

        this.add(panel);

        KeyListener keyListener = new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                int pos = area.getCaretPosition();
                switch (e.getKeyChar()) {
                    case '\t':
                        area.replaceRange("    ", pos - 1, pos);
                        area.setCaretPosition(pos + 3);
                        break;
                    case '\n':
                        int start, end;
                        for (start = pos - 2; start >= 0; start--) {
                            if (area.getText().charAt(start) == '\n')
                                break;
                        }
                        for (end = start + 1; end < pos; end++) {
                            if (area.getText().charAt(end) != ' ')
                                break;
                        }
                        String indent = "";
                        for (int k = 0; k < end - start - 1; k++)
                            indent = indent.concat(" ");
                        area.insert(indent, pos);
                        break;
                    case '(': area.insert(")", pos); area.setCaretPosition(pos); break;
                    case '<': area.insert(">", pos); area.setCaretPosition(pos); break;
                    case '{': area.insert("}", pos); area.setCaretPosition(pos); break;
                    case '[': area.insert("]", pos); area.setCaretPosition(pos); break;
                    case '\'': area.insert("\'", pos); area.setCaretPosition(pos); break;
                    case '\"': area.insert("\"", pos); area.setCaretPosition(pos); break;
                }
            }

            @Override
            public void keyPressed(KeyEvent e) { }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
                    close();
            }
        };

        this.addKeyListener(keyListener);
        area.addKeyListener(keyListener);
        btn.addKeyListener(keyListener);

        btn.addActionListener(e -> {
            if (e.getActionCommand().equals(CMD_OK))
                close();
        });
    }

    private void close() {
        if (wrapper != null) {
            String code = area.getText();
            Minecraft.getMinecraft().addScheduledTask(() -> wrapper.sendToServer(new NGTPacket(code)));
        }
        this.dispose();
    }

}
