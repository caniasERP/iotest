/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ias.iotviewer;

import static com.ias.iotviewer.UIHelper.logger;
import java.awt.Component;
import java.awt.Window;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.prefs.Preferences;
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
    private Socket socketForIntSerial;
    private Hashtable<Integer, Integer> readValues;
    private Hashtable<String, Float> readValuesANALOG;
    private Hashtable<Integer, Integer> writeValues;
    private Hashtable<Integer, Integer> pinStates;
    private PrintWriter out;
    private BufferedReader in;
    private Integer[] allPins = {2, 3, 17, 18, 21, 23, 25, 27, 6, 10, 11, 19, 22};
    private Integer[] pinsV2 = {2, 3, 17, 18, 21, 23, 25, 27, 6, 10, 11, 19, 22};
    private Integer[] optPins = {2, 3, 17, 18, 21, 23, 25, 27};
    final Integer[] interruptPins = {27, 25};
    final Integer[] doutPins = {6, 10, 11, 19};
    private String[] ioOutPins = {"AOUT1", "AOUT2", "PWMOUT"};
    List<Integer> pinsList = Arrays.asList(pinsV2);
    List<Integer> pinsList2 = Arrays.asList(allPins);
    static MyRunnable myRunnable;

    final static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger("UIHelper");
    Preferences prefs = Preferences.userNodeForPackage(com.ias.iotviewer.UIHelper.class);

    public UIHelper(JFrame frame, JPanel panel, Socket socket, Socket socketForIntSerial) {
        this.frame = frame;
        this.panel = panel;
        this.socket = socket;
        this.socketForIntSerial = socketForIntSerial;
        readValues = new Hashtable<Integer, Integer>();
        readValuesANALOG = new Hashtable<String, Float>();
        writeValues = new Hashtable<Integer, Integer>();
        pinStates = new Hashtable<Integer, Integer>();
        myRunnable = new MyRunnable(socketForIntSerial, this);

        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            startCheckInterruptAndSerial();
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
        readValuesANALOG.clear();
        writeValues.clear();

        sendToService();
        refreshUI();
    }

    public void sendAndReceive2() {

        Component myCA[] = panel.getComponents();

        readValues.clear();
        readValuesANALOG.clear();
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
                txtFld.setEditable(false);
                rbName = rbName + "R";
            } else {
                txtFld.setEditable(true);
                rbName = rbName + "W";
            }

            JRadioButton rb = (JRadioButton) getComponentByName(frame, rbName);
            rb.setSelected(true);

        }
    }

    public void prepareScreen2() {

        for (Integer c : pinStates.keySet()) {
            String compName = "txtPin" + c;
            if (pinsList.contains(c)) {

                JTextField txtFld = (JTextField) getComponentByName(frame, compName);

                if (txtFld != null) {
                    if (pinStates.get(c) != 1) {
                        txtFld.setEditable(false);
                        //txtFld.setFont(txtFld.getFont().deriveFont(Font.BOLD, 14f));
                    } else {
                        txtFld.setEditable(true);

                    }
                }
            } else {
                JTextField txtFld = (JTextField) getComponentByName(frame, compName);
                if (txtFld != null) {

                    txtFld.setEditable(false);
                    //txtFld.setFont(txtFld.getFont().deriveFont(Font.BOLD, 14f));
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

    public void checkAllPins() {

        try {

            //reading pins
            JSONObject ReadReqJson = new JSONObject();
            JSONArray ReadReqJsonArr = new JSONArray();
            JSONArray ReadReqJsonArr2 = new JSONArray();
            JSONArray ReadReqJsonArr3 = new JSONArray();

            for (int i = 0; i < allPins.length; i++) {

                ReadReqJsonArr.add(allPins[i]);
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

            logger.info("received json (for read) " + tcpReadResponse);

            JSONObject readJson = JSONObject.fromObject(tcpReadResponse);

            if (readJson.get("status").equals(0)) {
                JSONArray readArr = readJson.getJSONArray("values");
                if (readArr != null && readArr.size() > 0) {
                    for (int i = 0; i < readArr.size(); i++) {
                        readValues.put(allPins[i], (Integer) readArr.get(i));
                    }
                }
                
                readArr = readJson.getJSONArray("ioports");
                if (readArr != null && readArr.size() > 0) {
                    for (int i = 0; i < readArr.size(); i++) {
                        JSONObject ioJson = (JSONObject) readArr.get(i);
                        double d = (double) ioJson.getDouble("value");
                        float f = (float) d;
                        Float F = (Float) f;
                        readValuesANALOG.put((String) ioJson.get("ioport"), F);
                        //readValuesANALOG.put((String) ioJson.get("ioport"), (Float) ioJson.getDouble("value"));
                    }
                }

                refreshUI2();

            } else {
                JSONObject errJson = readJson.getJSONObject("error");
                logger.error("read error " + errJson.get("detail") + "-" + errJson.get("message"));
            }

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

            for (int i = 0; i < optPins.length; i++) {

                ReadReqJsonArr.add(optPins[i]);
            }

            JSONObject ReadreqItem2 = new JSONObject();
            ReadreqItem2.put("cmd", "get");
            ReadReqJsonArr2.add(ReadreqItem2);

            ReadreqItem2.put("pins", ReadReqJsonArr);

            ReadReqJsonArr3.add(ReadreqItem2);
            
            
            JSONObject ReadreqItem4 = new JSONObject();
            ReadreqItem4.put("cmd", "get");
            ReadreqItem4.put("ioport", "AIN1");
            ReadReqJsonArr3.add(ReadreqItem4);
            ReadreqItem4 = new JSONObject();
            ReadreqItem4.put("cmd", "get");
            ReadreqItem4.put("ioport", "AIN2");
            ReadReqJsonArr3.add(ReadreqItem4);
            ReadreqItem4 = new JSONObject();
            ReadreqItem4.put("cmd", "get");
            ReadreqItem4.put("ioport", "PWMIN1");
            ReadReqJsonArr3.add(ReadreqItem4);
            ReadreqItem4 = new JSONObject();
            ReadreqItem4.put("cmd", "get");
            ReadreqItem4.put("ioport", "PWMIN2");
            ReadReqJsonArr3.add(ReadreqItem4);

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
                txtFld.setEditable(false);
                JRadioButton rbR = (JRadioButton) getComponentByName(frame, "rb" + c + "R");
                rbR.setEnabled(false);

                JRadioButton rbW = (JRadioButton) getComponentByName(frame, "rb" + c + "W");
                rbW.setEnabled(false);

            }
        }
    }

    private void refreshUI2() {
        
        logger.info("refreshUI2");
        
            for (Integer c : readValues.keySet()) {

            if (pinsList2.contains(c)) {
                String compName = "txtPin" + c;

                JTextField txtFld = (JTextField) getComponentByName(frame, compName);

                if (txtFld != null) {
                    if (readValues.get(c) != -1) {

                        txtFld.setText(readValues.get(c).toString());
                    } else {
                        //txtFld.setFont(txtFld.getFont().deriveFont(Font.BOLD, 14f));
                        txtFld.setEditable(false);
                        //txtFld.setEnabled(false);

                    }
                }
            }
        }
        for (String c : readValuesANALOG.keySet()) {

            String compName = "txtPin" + c;

            JTextField txtFld = (JTextField) getComponentByName(frame, compName);

            if (txtFld != null) {
                if (readValuesANALOG.get(c) != -1) {

                    txtFld.setText(readValuesANALOG.get(c).toString());
                } else {
                    //txtFld.setFont(txtFld.getFont().deriveFont(Font.BOLD, 14f));
                    txtFld.setEditable(false);
                    //txtFld.setEnabled(false);

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
                readArr = readJson.getJSONArray("ioports");
                if (readArr != null && readArr.size() > 0) {
                    for (int i = 0; i < readArr.size(); i++) {
                        JSONObject ioJson = (JSONObject) readArr.get(i);
                        double d = (double) ioJson.getDouble("value");
                        float f = (float) d;
                        Float F = (Float) f;
                        readValuesANALOG.put((String) ioJson.get("ioport"), F);
                        //readValuesANALOG.put((String) ioJson.get("ioport"), (Float) ioJson.getDouble("value"));
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
                        readValues.put(optPins[i], (Integer) readArr.get(i));
                    }
                }
                readArr = readJson.getJSONArray("ioports");
                if (readArr != null && readArr.size() > 0) {
                    for (int i = 0; i < readArr.size(); i++) {
                        JSONObject ioJson = (JSONObject) readArr.get(i);
                        
                        double d = (double) ioJson.getDouble("value");
                        float f = (float) d;
                        Float F = (Float) f;
                        readValuesANALOG.put((String) ioJson.get("ioport"), F);
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

        myRunnable.doStop();

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

        String dataToWrite = "";

        JTextField txtFld = (JTextField) getComponentByName(frame, "serWTxt" + portNo);

        byte[] bytes = txtFld.getText().getBytes();
        try {
            dataToWrite = new String(bytes, StandardCharsets.UTF_8);
        } catch (Exception ex) {
            logger.error(ExceptionUtils.getFullStackTrace(ex));
        }
        WritereqItem = new JSONObject();
        WritereqItem.put("cmd", "serial_write");
        WritereqItem.put("port", portNo);
        WritereqItem.put("data", dataToWrite);
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
                        txtFld.setEditable(false);

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
                        txtFld.setEditable(true);

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

        logger.info("tcp request read from pin #" + pinNo + " " + ReqJson.toString());

        try {
            out.println(ReqJson.toString());
            String tcpReadResponse = in.readLine();

            logger.info("tcp response read from pin #" + pinNo + " " + tcpReadResponse);

            JSONObject readJson = JSONObject.fromObject(tcpReadResponse);

            if (readJson != null && readJson.get("status") != null && readJson.get("status").equals(0)) {
                JSONArray readArr = readJson.getJSONArray("pins");
                if (readArr != null && readArr.size() > 0) {
                    for (int i = 0; i < readArr.size(); i++) {
                        JSONObject jsItem = readArr.getJSONObject(i);
                        int value = (Integer) jsItem.get("value");

                        JTextField txtFld = (JTextField) getComponentByName(frame, "txtPin" + pinNo);
                        txtFld.setText(value + "");
                        txtFld.setEditable(false);

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
        String value = txtFld.getText();
        //byte[] bytes = value.getBytes();
       // String data = new String(bytes, StandardCharsets.US_ASCII);
        String vName = getComponentVariableName(txtFld);
        
        JSONObject ReqJson = new JSONObject();
        JSONArray ReqJsonArr = new JSONArray();
        JSONObject WritereqItem = new JSONObject();
        
        WritereqItem = new JSONObject();
        vName = vName.replace("txtPin", "");

        try {
            int pin = Integer.parseInt(vName);
            int sendData = Integer.parseInt(value);
            
            WritereqItem.put("cmd", "set");
            WritereqItem.put("pin", pin);
            WritereqItem.put("value", sendData);
        }
        catch (NumberFormatException e)
        {
            if (vName.equalsIgnoreCase("AOUT1") || vName.equalsIgnoreCase("AOUT2")) {
                Float sendData = Float.parseFloat(value);
            
                WritereqItem.put("cmd", "get");
                WritereqItem.put("ioport", vName);
                WritereqItem.put("value", sendData);
            }
        }
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

    public static void startCheckInterruptAndSerial() {

        Thread thread;
        thread = new Thread(myRunnable);
        thread.setName("IAS-Checking Interrupts and Serial Ports");
        thread.start();
    }
//used for reading data from serial port

    public static StringBuffer bytesToHex(byte[] bytes) {
        StringBuffer sb = new StringBuffer(bytes.length * 2);
        String tmp;
        for (int x = 0; x < bytes.length; x++) {
            tmp = Integer.toHexString(0xff & bytes[x]).toUpperCase();
            if (tmp.length() == 1) {
                sb.append('0');
            }
            sb.append(tmp);

        }
        return sb;
    }

    //used for writing data to serial port
    public static byte[] hexToBytes(String hex) {
        byte[] bytes = new byte[hex.length() / 2];
        for (int x = 0; x < bytes.length; x++) {
            StringBuffer token;
            token = new StringBuffer("00").append(hex.substring(x * 2, x * 2 + 2));
            bytes[x] = (byte) Integer.parseInt(token.toString(), 16);
        }
        return bytes;
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

    public void handleInterruptAndSerial(String response) {
        try {
            logger.info("received response (for checking Int/Serial) : " + response);
            JSONObject respJson = JSONObject.fromObject(response);

            if (!respJson.isEmpty()) {
                if (respJson.get("status").equals(0)) {
                    if (respJson.has("SerialA")) {
                        String data = respJson.getString("SerialA");
                        byte[] bytes = data.getBytes();
                        String dataEncoded = new String(bytes, StandardCharsets.UTF_8);

                        JTextField txtFld = (JTextField) getComponentByName(frame, "serTxt1");
                        txtFld.setText(dataEncoded);
                    }
                    if (respJson.has("SerialB")) {
                        String data = respJson.getString("SerialB");
                        byte[] bytes = data.getBytes();
                        String dataEncoded = new String(bytes, StandardCharsets.UTF_8);

                        JTextField txtFld = (JTextField) getComponentByName(frame, "serTxt2");
                        txtFld.setText(dataEncoded);
                    }
                     if (respJson.has("interrupts")) {
                        JSONArray jarr = respJson.getJSONArray("interrupts");
                        for (int i = 0; i < jarr.size(); i++) {
                            JSONObject interruptJson = (JSONObject) jarr.get(i);
                            int pin = (Integer)interruptJson.get("pin");
                            int value = (Integer)interruptJson.get("value");

                            if (pin == 27) {
                                JTextField txtFld = (JTextField) getComponentByName(frame, "txtPin27");    
                                txtFld.setText(value + "");
                            }
                            if (pin == 25) {
                                JTextField txtFld = (JTextField) getComponentByName(frame, "txtPin25");    
                                txtFld.setText(value + "");
                            }
                        }
                    }
                     else {
                        if (respJson.has("int7")) {
                            int value = (Integer) respJson.get("int7");
                            JTextField txtFld = (JTextField) getComponentByName(frame, "txtPin7");
                            txtFld.setText(value + "");
                        }
                        if (respJson.has("int12")) {
                            int value = (Integer) respJson.get("int12");
                            JTextField txtFld = (JTextField) getComponentByName(frame, "txtPin12");
                            txtFld.setText(value + "");
                        }
                     }
                } else {

                    JSONObject errJson = respJson.getJSONObject("error");
                    logger.error("check Interrupt/Serial error " + errJson.get("detail") + "-" + errJson.get("message"));

                }
            }
        } catch (Exception ex) {
            logger.error(ExceptionUtils.getFullStackTrace(ex));
        }
    }
}

class MyRunnable implements Runnable {

    private boolean doStop = false;
    private Socket socket;

    private BufferedReader in;
    private UIHelper uiHelper;
    final static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger("MyRunnable");

    public MyRunnable(Socket socket, UIHelper uiHelper) {
        try {
            this.socket = socket;
            this.uiHelper = uiHelper;
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (Exception ex) {
            logger.error(ExceptionUtils.getFullStackTrace(ex));
        }
    }

    public synchronized void doStop() {
        this.doStop = true;
    }

    private synchronized boolean keepRunning() {
        return this.doStop == false;
    }

    @Override
    public void run() {
        while (keepRunning()) {
            try {
                String tcpResponse = in.readLine();
                if (tcpResponse != null && !"".equals(tcpResponse)) {
                    uiHelper.handleInterruptAndSerial(tcpResponse);
                }
            } catch (Exception ex) {
                logger.error(ExceptionUtils.getFullStackTrace(ex));
            }

        }
    }
}
