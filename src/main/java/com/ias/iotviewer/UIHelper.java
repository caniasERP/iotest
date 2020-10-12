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
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import net.sf.json.*;
import org.apache.commons.lang.exception.ExceptionUtils;

/**
 *
 * @author METIN
 */
public class UIHelper {

    private JFrame frame;
    private JPanel panel;
    private Socket socket;
    private Hashtable<Integer, Integer> readValues;
    private Hashtable<Integer, Integer> writeValues;
    private Hashtable<Integer, Integer> pinStates;
    private PrintWriter out;
    private BufferedReader in;
    private Integer[] pinsV2 = {13, 19, 21, 22, 23, 24, 26, 32};
    List<Integer> pinsList = Arrays.asList(pinsV2);

    final static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger("UIHelper");

    public UIHelper(JFrame frame, JPanel panel, Socket socket) {
        this.frame = frame;
        this.panel = panel;
        this.socket = socket;
        readValues = new Hashtable<Integer, Integer>();
        writeValues = new Hashtable<Integer, Integer>();
        pinStates = new Hashtable<Integer, Integer>();
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (Exception ex) {
            logger.error(ex);
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

        sendToService();
        refreshUI();
    }

    public void sendAndReceive2() {

        Component myCA[] = panel.getComponents();

        readValues.clear();
        writeValues.clear();

        sendToService2();
        refreshUI2();
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
            logger.error(ex);
        }
    }

    public void getPinStates2() {
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
                prepareScreen2();
            } else {
                JSONObject errJsonPre = RespJson.getJSONObject("error");
                logger.error("checking pins state error: " + errJsonPre.get("detail") + "-" + errJsonPre.get("message"));
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error(ex);
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

    public void prepareScreen2() {
       
        for (Integer c : pinStates.keySet()) {

            if (pinsList.contains(c)) {
                String compName = "txtPin" + c;
             
                JTextField txtFld = (JTextField) getComponentByName(frame, compName);

                if (txtFld != null) {
                    if (pinStates.get(c) != 1) {
                        txtFld.setEnabled(false);

                    } else {
                        txtFld.setEnabled(true);

                    }
                }
            }

        }
    }

    private void sendToService() {

        try {

            //reading pins
            JSONObject ReadReqJson = new JSONObject();
            JSONArray ReadReqJsonArr = new JSONArray();
            JSONArray ReadReqJsonArr2 = new JSONArray();
            JSONArray ReadReqJsonArr3 = new JSONArray();

            for (int i = 0; i < 40; i++) {

                ReadReqJsonArr.add(i + 1);
            }

            JSONObject ReadreqItem2 = new JSONObject();
            ReadreqItem2.put("cmd", "get");
            ReadReqJsonArr2.add(ReadreqItem2);

            ReadreqItem2.put("pins", ReadReqJsonArr);

            ReadReqJsonArr3.add(ReadreqItem2);

            ReadReqJson.put("commands", ReadReqJsonArr3);

            logger.info("sent json (for read) : " + ReadReqJson.toString());

            out.println(ReadReqJson.toString());
            String tcpReadResponse = in.readLine();

            handleReadResp(tcpReadResponse);

        } catch (Exception ex) {
            logger.error(ex);
        }

    }

    private void sendToService2() {

        try {

            //reading pins
            JSONObject ReadReqJson = new JSONObject();
            JSONArray ReadReqJsonArr = new JSONArray();
            JSONArray ReadReqJsonArr2 = new JSONArray();
            JSONArray ReadReqJsonArr3 = new JSONArray();

            for (int i = 0; i < 40; i++) {

                ReadReqJsonArr.add(i + 1);
            }

            JSONObject ReadreqItem2 = new JSONObject();
            ReadreqItem2.put("cmd", "get");
            ReadReqJsonArr2.add(ReadreqItem2);

            ReadreqItem2.put("pins", ReadReqJsonArr);

            ReadReqJsonArr3.add(ReadreqItem2);

            JSONObject ReadreqItem3 = new JSONObject();
            ReadreqItem3.put("cmd", "serial_read");
            ReadReqJsonArr3.add(ReadreqItem3);
            ReadReqJson.put("commands", ReadReqJsonArr3);

            logger.info("sent json (for read) : " + ReadReqJson.toString());

            out.println(ReadReqJson.toString());
            String tcpReadResponse = in.readLine();

            handleReadResp2(tcpReadResponse);

        } catch (Exception ex) {
            logger.error(ex);
        }

    }

    private void refreshUI() {
        for (Integer c : readValues.keySet()) {

            String compName = "txtPin" + c;

            if (readValues.get(c) != -1) {
                JTextField txtFld = (JTextField) getComponentByName(frame, compName);
                txtFld.setText(readValues.get(c).toString());
            } else {
                JTextField txtFld = (JTextField) getComponentByName(frame, compName);
                txtFld.setEnabled(false);
                JRadioButton rbR = (JRadioButton) getComponentByName(frame, "rb" + c + "R");
                rbR.setEnabled(false);

                JRadioButton rbW = (JRadioButton) getComponentByName(frame, "rb" + c + "W");
                rbW.setEnabled(false);

            }

        }
    }

    private void refreshUI2() {
        for (Integer c : readValues.keySet()) {

            if (pinsList.contains(c)) {
                String compName = "txtPin" + c;

                JTextField txtFld = (JTextField) getComponentByName(frame, compName);

                if (txtFld != null) {
                    if (readValues.get(c) != -1) {

                        txtFld.setText(readValues.get(c).toString());
                    } else {

                        txtFld.setEnabled(false);

                    }
                }
            }
        }
    }

    private void handleReadResp(String resp) {
        logger.info("received json (for read) " + resp);

        try {
            JSONObject readJson = JSONObject.fromObject(resp);

            if (readJson.get("status").equals(0)) {
                JSONArray readArr = readJson.getJSONArray("values");
                if (readArr != null && readArr.size() > 0) {
                    for (int i = 0; i < readArr.size(); i++) {
                        readValues.put(i + 1, (Integer) readArr.get(i));
                    }
                }
            } else {
                JSONObject errJson = readJson.getJSONObject("error");
                logger.error("read error " + errJson.get("detail") + "-" + errJson.get("message"));
            }
        } catch (Exception ex) {
            logger.error(ex);
        }

    }

    private void handleReadResp2(String resp) {
        logger.info("received json (for read) " + resp);

        try {
            JSONObject readJson = JSONObject.fromObject(resp);

            if (readJson.get("status").equals(0)) {
                JSONArray readArr = readJson.getJSONArray("values");
                if (readArr != null && readArr.size() > 0) {
                    for (int i = 0; i < readArr.size(); i++) {
                        readValues.put(i + 1, (Integer) readArr.get(i));
                    }
                }

                //serial port read
                if (readJson.get("data1") != null) {
                    String data1 = readJson.getString("data1");
                    JTextField txt1 = getComponentByName(frame, "serTxt1");
                    txt1.setText(data1);
                }

                if (readJson.get("data2") != null) {
                    String data2 = readJson.getString("data2");
                    JTextField txt2 = getComponentByName(frame, "serTxt2");
                    txt2.setText(data2);
                }
                //serial port read
            } else {
                JSONObject errJson = readJson.getJSONObject("error");
                logger.error("read error " + errJson.get("detail") + "-" + errJson.get("message"));
            }
        } catch (Exception ex) {
            logger.error(ex);
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
            logger.error(ex);
        }
    }

    public void closeConnection() {

        if (socket != null) {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception ex) {
                    logger.error(ex);
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (Exception ex) {
                    logger.error(ex);
                }
            }

            try {
                socket.close();
            } catch (Exception ex) {
                logger.error(ExceptionUtils.getFullStackTrace(ex));
            }
        }
    }

    public void actionHandlerRB(Object obj) {
        JRadioButton rb = (JRadioButton) obj;

        String vName = getComponentVariableName(rb);
        vName = vName.replace("rb", "");
        String lastChar = vName.substring(vName.length() - 1);
        int pinNo = Integer.parseInt(vName.replace(lastChar, ""));

        if (lastChar.equals("R")) {
            readActionRB(pinNo);
        } else {
            writeActionRB(pinNo);
        }
    }

    public void writeToSerialPort(int portNo) {
        JSONObject ReqJson = new JSONObject();
        JSONArray ReqJsonArr = new JSONArray();
        JSONObject WritereqItem = new JSONObject();

        JTextField txtFld = (JTextField) getComponentByName(frame, "serTxt" + portNo);

        WritereqItem = new JSONObject();
        WritereqItem.put("cmd", "serial_write");
        WritereqItem.put("port", portNo);
        WritereqItem.put("data", txtFld.getText());
        ReqJsonArr.add(WritereqItem);
        ReqJson.put("commands", ReqJsonArr);

        txtFld.setText("");
        JButton btn = getComponentByName(frame, "btnSendToSerial" + portNo);
        btn.setEnabled(false);

        logger.info("tcp request write to serial port " + ReqJson.toString());

        try {
            out.println(ReqJson.toString());
            String tcpWriteResponse = in.readLine();
            logger.info("tcp response write to pin " + tcpWriteResponse);
            JSONObject writeJson = JSONObject.fromObject(tcpWriteResponse);

            if (writeJson.get("status").equals(0)) {

                logger.info("write to serial port " + portNo + " ok");

            } else {
                JSONObject errJson = writeJson.getJSONObject("error");
                logger.error("write to serial port error " + errJson.get("detail") + "-" + errJson.get("message"));
            }

        } catch (Exception ex) {
            logger.error(ExceptionUtils.getFullStackTrace(ex));
        }
    }

    private void readActionRB(int pin) {

        JSONObject ReqJson = new JSONObject();
        JSONArray ReqJsonArr = new JSONArray();
        JSONObject ReadreqItem = new JSONObject();
        ReadreqItem.put("cmd", "mode");
        ReadreqItem.put("pin", pin);
        ReadreqItem.put("value", 0);
        ReqJsonArr.add(ReadreqItem);
        ReadreqItem = new JSONObject();
        ReadreqItem.put("cmd", "get");
        ReadreqItem.put("pin", pin);
        ReqJsonArr.add(ReadreqItem);
        ReqJson.put("commands", ReqJsonArr);

        logger.info("tcp request read from pin " + ReqJson.toString());

        try {
            out.println(ReqJson.toString());
            String tcpReadResponse = in.readLine();

            logger.info("tcp response read from pin " + ReqJson.toString());

            JSONObject readJson = JSONObject.fromObject(tcpReadResponse);

            if (readJson.get("status").equals(0)) {
                JSONArray readArr = readJson.getJSONArray("pins");
                if (readArr != null && readArr.size() > 0) {
                    for (int i = 0; i < readArr.size(); i++) {
                        JSONObject jsItem = readArr.getJSONObject(i);
                        int value = (Integer) jsItem.get("value");

                        JTextField txtFld = (JTextField) getComponentByName(frame, "txtPin" + pin);
                        txtFld.setText(value + "");
                        txtFld.setEnabled(false);

                    }
                }
            } else {
                JSONObject errJson = readJson.getJSONObject("error");
                logger.error("read error " + errJson.get("detail") + "-" + errJson.get("message"));
            }

        } catch (Exception ex) {
            logger.error(ExceptionUtils.getFullStackTrace(ex));
        }
    }

    private void writeActionRB(int pin) {

        JTextField txtFld = (JTextField) getComponentByName(frame, "txtPin" + pin);

        JSONObject ReqJson = new JSONObject();
        JSONArray ReqJsonArr = new JSONArray();
        JSONObject ReadreqItem = new JSONObject();
        ReadreqItem.put("cmd", "mode");
        ReadreqItem.put("pin", pin);
        ReadreqItem.put("value", 1);
        ReqJsonArr.add(ReadreqItem);
        ReadreqItem = new JSONObject();
        ReadreqItem.put("cmd", "get");
        ReadreqItem.put("pin", pin);
        ReqJsonArr.add(ReadreqItem);

        ReqJson.put("commands", ReqJsonArr);

        logger.info("tcp request write to pin " + ReqJson.toString());

        try {
            out.println(ReqJson.toString());
            String tcpWriteResponse = in.readLine();
            logger.info("tcp response write to pin " + tcpWriteResponse);
            JSONObject writeJson = JSONObject.fromObject(tcpWriteResponse);

            if (writeJson.get("status").equals(0)) {

                JSONArray readArr = writeJson.getJSONArray("pins");
                if (readArr != null && readArr.size() > 0) {
                    for (int i = 0; i < readArr.size(); i++) {
                        JSONObject jsItem = readArr.getJSONObject(i);
                        int value = (Integer) jsItem.get("value");

                        txtFld.setText(value + "");
                        txtFld.setEnabled(true);

                    }
                }

                logger.info("write to pin ok");

            } else {
                JSONObject errJson = writeJson.getJSONObject("error");
                logger.error("write to pin error " + errJson.get("detail") + "-" + errJson.get("message"));
            }

        } catch (Exception ex) {
            logger.error(ExceptionUtils.getFullStackTrace(ex));
        }
    }

    public void readSinglePin(int pinNo) {
       

        JSONObject ReqJson = new JSONObject();
        JSONArray ReqJsonArr = new JSONArray();
        JSONObject ReadreqItem = new JSONObject();
        ReadreqItem.put("cmd", "get");
        ReadreqItem.put("pin", pinNo);
        ReqJsonArr.add(ReadreqItem);
        ReqJson.put("commands", ReqJsonArr);

        logger.info("tcp request read from pin #"+pinNo +" "+ ReqJson.toString());

        try {
            out.println(ReqJson.toString());
            String tcpReadResponse = in.readLine();

            logger.info("tcp response read from pin #"+pinNo+" " + tcpReadResponse);

            JSONObject readJson = JSONObject.fromObject(tcpReadResponse);

            if (readJson!=null && readJson.get("status")!=null && readJson.get("status").equals(0)) {
                JSONArray readArr = readJson.getJSONArray("pins");
                if (readArr != null && readArr.size() > 0) {
                    for (int i = 0; i < readArr.size(); i++) {
                        JSONObject jsItem = readArr.getJSONObject(i);
                        int value = (Integer) jsItem.get("value");

                        JTextField txtFld = (JTextField) getComponentByName(frame, "txtPin" + pinNo);
                        txtFld.setText(value + "");
                        txtFld.setEnabled(false);

                    }
                }
            } else {
                JSONObject errJson = readJson.getJSONObject("error");
                logger.error("read error " + errJson.get("detail") + "-" + errJson.get("message"));
            }
        } catch (Exception ex) {
            logger.error(ExceptionUtils.getFullStackTrace(ex));
        }

    }

    public void sendWriteOnChange(Object obj) {

        JTextField txtFld = (JTextField) obj;
        int value = Integer.parseInt(txtFld.getText());
        String vName = getComponentVariableName(txtFld);
        int pin = Integer.parseInt(vName.replace("txtPin", ""));

        JSONObject ReqJson = new JSONObject();
        JSONArray ReqJsonArr = new JSONArray();
        JSONObject WritereqItem = new JSONObject();

        WritereqItem = new JSONObject();
        WritereqItem.put("cmd", "set");
        WritereqItem.put("pin", pin);
        WritereqItem.put("value", value);
        ReqJsonArr.add(WritereqItem);
        ReqJson.put("commands", ReqJsonArr);

        logger.info("tcp request write to pin " + ReqJson.toString());

        try {
            out.println(ReqJson.toString());
            String tcpWriteResponse = in.readLine();
            logger.info("tcp response write to pin " + tcpWriteResponse);
            JSONObject writeJson = JSONObject.fromObject(tcpWriteResponse);

            if (writeJson.get("status").equals(0)) {

                logger.info("write to pin ok");

            } else {
                JSONObject errJson = writeJson.getJSONObject("error");
                logger.error("write to pin error " + errJson.get("detail") + "-" + errJson.get("message"));
            }

        } catch (Exception ex) {
            logger.error(ExceptionUtils.getFullStackTrace(ex));
        }

    }

    private void showInfoMsg(String message) {
        JOptionPane.showMessageDialog(frame, message, "Information", JOptionPane.INFORMATION_MESSAGE);
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
