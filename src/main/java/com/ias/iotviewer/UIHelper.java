/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ias.iotviewer;

import java.awt.Component;
import java.awt.Container;
import java.awt.Window;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.net.Socket;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import net.sf.json.*;

/**
 *
 * @author METIN
 */
public class UIHelper {

    private JFrame frame;
    private JPanel panel;
    private Socket socket;
    private Hashtable<Integer, String> readValues;
    private Hashtable<Integer, String> writeValues;
    private Hashtable<Integer, Integer> pinStates;
    private PrintWriter out;
    private BufferedReader in;

    final static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger("UIHelper");

    public UIHelper(JFrame frame, JPanel panel, Socket socket) {
        this.frame = frame;
        this.panel = panel;
        this.socket = socket;
        readValues = new Hashtable<Integer, String>();
        writeValues = new Hashtable<Integer, String>();
        pinStates = new Hashtable<Integer, Integer>();
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (Exception ex) {
            logger.error(ex.toString());
        }

    }

    public String getSelectedButtonText(ButtonGroup buttonGroup) {
        for (Enumeration<AbstractButton> buttons = buttonGroup.getElements(); buttons.hasMoreElements();) {
            AbstractButton button = buttons.nextElement();

            if (button.isSelected()) {
                return button.getText();
            }
        }

        return null;
    }

    public void sendAndReceive() {

        Component myCA[] = panel.getComponents();

        readValues.clear();
        writeValues.clear();

        for (int i = 0; i < myCA.length; i++) {

            if (myCA[i] instanceof JRadioButton) {
                JRadioButton rb = (JRadioButton) myCA[i];
                if (rb.isSelected()) {
                    String vName = getComponentVariableName(rb);
                    vName = vName.replace("rb", "");
                    String lastChar = vName.substring(vName.length() - 1);
                    int pinNo = Integer.parseInt(vName.replace(lastChar, ""));

                    if (lastChar.equals("R")) {
                        readValues.put(pinNo, "");
                    } else {
                        JTextField txtFld = (JTextField) getComponentByName(frame, "txtPin" + pinNo);
                        if (!txtFld.getText().equals("")) {
                            writeValues.put(pinNo, txtFld.getText());
                        }
                    }

                }
            }

        }

        sendToService();
        refreshUI();
    }

    public void getPinStates() {
        JSONObject ReqJson = new JSONObject();
        JSONArray ReqJsonArr = new JSONArray();
        JSONArray ReqJsonArr2 = new JSONArray();
        JSONArray ReqJsonArr3 = new JSONArray();
        JSONObject RespJson;

        for (int i = 0; i < 40; i++) {

            ReqJsonArr.add(i + 1);
        }

        JSONObject reqItem2 = new JSONObject();
        reqItem2.put("cmd", "mode");
        ReqJsonArr2.add(reqItem2);

        reqItem2.put("pins", ReqJsonArr);

        ReqJsonArr3.add(reqItem2);

        ReqJson.put("commands", ReqJsonArr3);

        logger.info("checking pins state : sent json : " + ReqJson.toString());

        try {

            out.println(ReqJson.toString());
            String tcpResponse = in.readLine();
            logger.info("checking pins state : received tcp response " + tcpResponse);
            RespJson = JSONObject.fromObject(tcpResponse);

            logger.info("checking pins state : received json : " + RespJson.toString());

            if (RespJson.get("status").equals(0)) {
                JSONArray jarr = RespJson.getJSONArray("modes");
                for (int i = 0; i < jarr.size(); i++) {
                    pinStates.put(i + 1, (Integer) jarr.get(i));
                }
                prepareScreen();
            } else {
                JSONObject errJsonPre = RespJson.getJSONObject("error");
                logger.error("checking pins state error: " + errJsonPre.get("detail") + "-" + errJsonPre.get("message"));
            }

        } catch (Exception ex) {
            logger.error(ex.toString());
        }
    }

    public void prepareScreen() {

        for (Integer c : pinStates.keySet()) {

            String compName = "txtPin" + c;
            String rbName = "rb" + c;
            JTextField txtFld = (JTextField) getComponentByName(frame, compName);

            if (pinStates.get(c) != 1) {
                txtFld.setEnabled(false);
                rbName = rbName + "R";
            } else {
                txtFld.setEnabled(true);
                rbName = rbName + "W";
            }

            JRadioButton rb = (JRadioButton) getComponentByName(frame, rbName);
            rb.setSelected(true);

        }
    }

    private void sendToService() {

        //setting mode per pin
        JSONObject preReqJson = new JSONObject();
        JSONArray preReqJsonArr = new JSONArray();
        JSONObject preRespJson;

        JSONObject readReqJson = new JSONObject();
        JSONArray readReqJsonArr = new JSONArray();

        JSONObject writeReqJson = new JSONObject();
        JSONArray writeReqJsonArr = new JSONArray();

        for (Integer c : readValues.keySet()) {

            if (pinStates.get(c) == 1) {
                JSONObject reqItem = new JSONObject();
                reqItem.put("cmd", "mode");
                reqItem.put("pin", c);
                reqItem.put("value", 0);
                preReqJsonArr.add(reqItem);
            }
        }

        for (Integer c : writeValues.keySet()) {
            if (pinStates.get(c) != 1) {
            JSONObject reqItem = new JSONObject();
            reqItem.put("cmd", "mode");
            reqItem.put("pin", c);
            reqItem.put("value", 1);
            preReqJsonArr.add(reqItem);
            }
        }

        preReqJson.put("commands", preReqJsonArr);

        logger.info("sent json : " + preReqJson.toString());

        try {

            out.println(preReqJson.toString());
            String tcpResponse = in.readLine();
            preRespJson = JSONObject.fromObject(tcpResponse);

            logger.info("received json : " + preRespJson.toString());

            if (preRespJson.get("status").equals(0)) {

                //reading pins
                for (Integer c : readValues.keySet()) {
                    JSONObject reqItem = new JSONObject();
                    reqItem.put("cmd", "get");
                    reqItem.put("pin", c);
                    readReqJsonArr.add(reqItem);
                }

                readReqJson.put("commands", readReqJsonArr);

                logger.info("sent json (for read) : " + readReqJson.toString());

                out.println(readReqJson.toString());
                String tcpReadResponse = in.readLine();

                handleReadResp(tcpReadResponse);

                //writing to pins
                for (Integer c : writeValues.keySet()) {
                    JSONObject reqItem = new JSONObject();
                    reqItem.put("cmd", "set");
                    reqItem.put("pin", c);
                    reqItem.put("value", writeValues.get(c));
                    writeReqJsonArr.add(reqItem);
                }

                writeReqJson.put("commands", writeReqJsonArr);

                out.println(writeReqJson.toString());
                String tcpWriteResponse = in.readLine();

                handleWriteResp(tcpWriteResponse);

            } else {
                JSONObject errJsonPre = preRespJson.getJSONObject("error");
                logger.error(errJsonPre.get("detail") + "-" + errJsonPre.get("message"));
            }

        } catch (Exception ex) {
            logger.error(ex.toString());
        }

    }

    private void refreshUI() {
        for (Integer c : readValues.keySet()) {

            String compName = "txtPin" + c;

            JTextField txtFld = (JTextField) getComponentByName(frame, compName);
            txtFld.setText(readValues.get(c));

        }
    }

    private void handleReadResp(String resp) {
        logger.info("received json (for read) " + resp);

        try {
            JSONObject readJson = JSONObject.fromObject(resp);

            if (readJson.get("status").equals(0)) {
                JSONArray readArr = readJson.getJSONArray("pins");
                if (readArr != null && readArr.size() > 0) {
                    for (int i = 0; i < readArr.size(); i++) {
                        JSONObject jsItem = readArr.getJSONObject(i);
                        int pinNO = Integer.parseInt(jsItem.getString("pin"));
                        String value = jsItem.getString("value");
                        readValues.put(pinNO, value);
                    }
                }
            } else {
                JSONObject errJson = readJson.getJSONObject("error");
                logger.error("read error " + errJson.get("detail") + "-" + errJson.get("message"));
            }
        } catch (Exception ex) {
            logger.error(ex.toString());
        }

    }

    private void handleWriteResp(String resp) {
        logger.info("received json (for write) " + resp);

        try {
            JSONObject readJson = JSONObject.fromObject(resp);

            if (readJson.get("status").equals(0)) {
                logger.info("write ok");
            } else {
                JSONObject errJson = readJson.getJSONObject("error");
                logger.error("write error " + errJson.get("detail") + "-" + errJson.get("message"));
            }
        } catch (Exception ex) {
            logger.error(ex.toString());
        }
    }

    public void closeConnection() {

        if (socket != null) {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception ex) {
                    logger.error(ex.toString());
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (Exception ex) {
                    logger.error(ex.toString());
                }
            }

            try {
                socket.close();
            } catch (Exception ex) {
                logger.error(ex.toString());
            }
        }
    }

    static public String getComponentVariableName(Object object) {

        if (object instanceof Component) {
            final Component component = (Component) object;
            final StringBuilder sb = new StringBuilder();

            // find the form where the variable name would be likely to exist
            final Component parentForm = getParentForm(component);

            // loop through all of the class fields on that form
            for (Field field : parentForm.getClass().getDeclaredFields()) {

                try {
                    // let us look at private fields, please
                    field.setAccessible(true);

                    // get a potential match
                    final Object potentialMatch = field.get(parentForm);

                    // compare it
                    if (potentialMatch == component) {

                        // return the name of the variable used
                        // to hold this component
                        if (sb.length() > 0) {
                            sb.append(",");
                        }
                        sb.append(field.getName());
                    }

                } catch (SecurityException | IllegalArgumentException
                        | IllegalAccessException ex) {

                    // ignore exceptions
                }
            }

            if (sb.length() > 0) {
                return sb.toString();
            }
        }

        // if we get here, we're probably trying to find the form
        // itself, in which case it may be more useful to print
        // the class name (MyJFrame) than the AWT-assigned name
        // of the form (frame0)
        final String className = object.getClass().getName();
        final String[] split = className.split("\\.");
        final int lastIndex = split.length - 1;
        return (lastIndex >= 0) ? split[lastIndex] : className;

    }

    /**
     * traverses up the component tree to find the top, which i assume is the
     * dialog or frame upon which this component lives.
     *
     * @param sourceComponent
     * @return top level parent component
     */
    static public Component getParentForm(Component sourceComponent) {
        while (sourceComponent.getParent() != null) {
            sourceComponent = sourceComponent.getParent();
        }
        return sourceComponent;
    }

    static public <T extends Component> T getComponentByName(Window window, String name) {

        // loop through all of the class fields on that form
        for (Field field : window.getClass().getDeclaredFields()) {

            try {
                // let us look at private fields, please
                field.setAccessible(true);

                // compare the variable name to the name passed in
                if (name.equals(field.getName())) {

                    // get a potential match (assuming correct &lt;T&gt;ype)
                    final Object potentialMatch = field.get(window);

                    // cast and return the component
                    return (T) potentialMatch;
                }

            } catch (SecurityException | IllegalArgumentException
                    | IllegalAccessException ex) {

                // ignore exceptions
            }

        }

        // no match found
        return null;
    }
}
