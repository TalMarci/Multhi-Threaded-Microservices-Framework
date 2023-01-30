package bgu.spl.mics.application;

import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.passiveObjects.Diary;
import bgu.spl.mics.application.passiveObjects.Ewok;
import bgu.spl.mics.application.passiveObjects.Ewoks;
import bgu.spl.mics.application.services.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.util.concurrent.CountDownLatch;


/** This is the Main class of the application. You should parse the input file,
 * create the different components of the application, and run the system.
 * In the end, you should output a JSON.
 */
public class Main {
	public static void main(String[] args) throws IOException, InterruptedException {

		Gson gson = new Gson();
		InputJson inputjson = gson.fromJson(new FileReader(args[0]), InputJson.class);//read the json file
		CountDownLatch latch=new CountDownLatch(4);//Initialize the CountDownLatch with a size of 4 (will be used on 4 microservices).
		Ewoks ewoks = Ewoks.getInstance();//Obtain the Ewoks singleton instance
		ewoks.setEwokList(inputjson.getEwoks());//set the Ewoks using the Ewoks we was given as an input from the json
		Diary diary = Diary.getInstance();//Obtain the Diary singleton instance

		/*
		This part creates the MicroServices (all but Leia), sets a latch field (that is being used to make sure that they all finished their initialization before we initialize Leia)
		 */
		HanSoloMicroservice HanSolo = new HanSoloMicroservice();
		HanSolo.setLatch(latch);
		Thread T1 = new Thread(HanSolo);
		C3POMicroservice C3PO = new C3POMicroservice();
		C3PO.setLatch(latch);
		Thread T2 = new Thread(C3PO);
		LandoMicroservice Lando = new LandoMicroservice(inputjson.getLando());
		Lando.setLatch(latch);
		Thread T3 = new Thread(Lando);
		R2D2Microservice R2D2 = new R2D2Microservice(inputjson.getR2D2());
		R2D2.setLatch(latch);
		Thread T4 = new Thread(R2D2);

		//Start their initialization, and wait until they finish before we proceed to initialize Leia.
		T1.start();
		T2.start();
		T3.start();
		T4.start();
		latch.await();

		//We now initialize Leia and she can now start send AttackEvents
		Thread T5 = new Thread(new LeiaMicroservice(inputjson.getAttacks()));
		T5.start();

		//Wait for all threads to finish their run before we write the diary.
		T1.join();
		T2.join();
		T3.join();
		T4.join();
		T5.join();

		//write the diary json file.
		Gson json = new GsonBuilder().setPrettyPrinting().create();
		FileWriter writer = new FileWriter(args[1]);
		json.toJson(diary,writer);
		writer.flush();
		writer.close();
	}
}
