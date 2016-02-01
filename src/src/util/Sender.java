package src.util;

import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;

//immainPort edu.wpi.first.wpilibj.Jaguar;

public class Sender {
	SerialPort mainPort;

	public Sender() {
		mainPort = initializePort();
	}

	public void runTest() {
		String[] mainPortNames = SerialPortList.getPortNames();
		if (mainPortNames.length == 0) {
	    	System.out.println(":(");
	    }else {
	    	System.out.println(":D :D :D :D PORTS FOUND! :D :D :D :D");
			for(int i = 0; i < mainPortNames.length; i++){
		        System.out.println(mainPortNames[i]);
		    }
	    }
	}

	public SerialPort initializePort() {
		return new SerialPort((SerialPortList.getPortNames())[0]);//new SerialPort("COM1");
	}

	public void sendData(byte[] bytebuffer) {
		try {
			mainPort.openPort();
			mainPort.setParams(9600, 8, 1, 0);
			mainPort.writeBytes(bytebuffer);
			mainPort.closePort();
		} catch (SerialPortException e) {
			e.printStackTrace();
		}
	}

	public void emptyPortInput() {
		try {
			mainPort.readBytes(mainPort.getInputBufferBytesCount());
		} catch (SerialPortException e) {
			e.printStackTrace();
		}
	}

	public void sendDoubles(double[] doubles) {
		byte[] byteArray = new byte[doubles.length * 8];// 8 bytes per double
		for(int i = 0; i < 8; i++) {
			byte[] convertedDouble = Converter.doubleToBytes(doubles[i]);
			for(int j = 0; j < convertedDouble.length; j++) {
				byteArray[(i * 8) + j] = convertedDouble[j];
			}
		}
		sendData(byteArray);
	}

	public boolean isEmpty() {
		/*
		 * When sending data across, there shouldn't be more than one "frame" of data
		 * sent. 
		 * This ensures that more data cannot be sent before it is read.
		 */
		try {
			return (mainPort.getInputBufferBytesCount() == 0);
		} catch (SerialPortException e) {
			e.printStackTrace();
		}
		return false;
	}
}