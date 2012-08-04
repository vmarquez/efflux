package com.biasedbit.efflux.session;

import com.biasedbit.efflux.packet.DataPacket;
import com.biasedbit.efflux.participant.RtpParticipant;
import com.biasedbit.efflux.participant.RtpParticipantInfo;
import org.junit.After;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.io.*;

import static org.junit.Assert.*;

public class FullFileSingleParticipantSessionFunctionalTest {
	private SingleParticipantSession session1;
    private SingleParticipantSession session2;

    private static String FILENAME = "/smalltest.txt";
    
    @After
    public void tearDown() {
        if (this.session1 != null) {
            this.session1.terminate();
        }

        if (this.session2 != null) {
            this.session2.terminate();
        }
    }

    @Test
    public void testSendAndReceiveHere() {
        final CountDownLatch latch = new CountDownLatch(2);
        System.out.println("I'm testing SEND AND RECEIVE");
        RtpParticipant local1 = RtpParticipant.createReceiver(new RtpParticipantInfo(1), "127.0.0.1", 6000, 6001);
        RtpParticipant remote1 = RtpParticipant.createReceiver(new RtpParticipantInfo(2), "127.0.0.1", 7000, 7001);
        this.session1 = new SingleParticipantSession("Session1", 8, local1, remote1);
        assertTrue(this.session1.init());
        this.session1.addDataListener(new RtpSessionDataListener() {
            private int count = 0;
        	@Override
            public void dataPacketReceived(RtpSession session, RtpParticipantInfo participant, DataPacket packet) {
        		
                System.err.println("Session1 received packet # " + (count++) + ", data: " + new String(packet.getDataAsArray()) + "(session: " + session.getId() + ")");
            }
        });

        RtpParticipant local2 = RtpParticipant.createReceiver(new RtpParticipantInfo(2), "127.0.0.1", 7000, 7001);
        RtpParticipant remote2 = RtpParticipant.createReceiver(new RtpParticipantInfo(1), "127.0.0.1", 6000, 6001);
        this.session2 = new SingleParticipantSession("Session2", 8, local2, remote2);
        assertTrue(this.session2.init());
        this.session2.addDataListener(new RtpSessionDataListener() {
            private int count = 0;
        	@Override
            public void dataPacketReceived(RtpSession session, RtpParticipantInfo participant, DataPacket packet) {
                System.err.println("Session2 received packet # " + (count++) + ", data: " + new String(packet.getDataAsArray()) + "(session: " + session.getId() + ")");
            }
        });

        System.out.println("I'm here!");
        try {
        	
        	InputStream filestream = new FileInputStream(FILENAME);
        	
        	byte[] bytes = new byte[512];
        	int seqNumber = 1;
        	int bytesread = filestream.read(bytes);
        	while (bytesread != -1) {
        		DataPacket packet = new DataPacket();
            	packet.setData(bytes);
            	packet.setSequenceNumber(seqNumber++);
            	session1.sendDataPacket(packet);
        		bytesread = filestream.read(bytes);
        	}
        	System.out.println("OK i sent everything");
        	Thread.sleep(8000);
        	System.out.println("i'm done sleeping");
        } catch (Exception e) {
        	System.out.println("OK exception = " + e);
            fail("Exception caught: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }


}
