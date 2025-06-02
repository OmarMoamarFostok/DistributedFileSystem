package tests;

import client.ClientApp;
import shared.CoordinatorInterface;
import shared.DownloadInstruction;
import shared.UploadInstruction;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class RMIRaceTest {
    CoordinatorInterface coordinator;
    public RMIRaceTest(CoordinatorInterface coordinator) {
        this.coordinator = coordinator;
    }
    public void downloadFromNodeRaceTest(String department, String filename) throws Exception {
        String token = coordinator.login("omar","123");

        int threadsCount = 50;
        AtomicInteger checkSum = new AtomicInteger(0);
        ExecutorService executor = Executors.newFixedThreadPool(threadsCount);
        CountDownLatch latch = new CountDownLatch(threadsCount);


        DownloadInstruction downloadInstr = (DownloadInstruction) coordinator.downloadFile(department,filename);

        if(downloadInstr==null) {
            return;
        }


        for(int i=0;i<threadsCount;i++) {
            executor.submit(() -> {
                try {
//                    Thread.sleep(1000);
                    System.out.println("Running on thread: " + Thread.currentThread().getName());
                    if(downloadInstr.targetNode.download(downloadInstr.token)!=null) {
                        checkSum.incrementAndGet();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();  // wait for all downloads to finish
        executor.shutdown();
        System.out.println("Successful downloads: " + checkSum.get());
    }
    public void deleteRaceTest(String department, String filename) throws Exception{
        String token = coordinator.login("omar2","123");
        int threadsCount = 50;
        AtomicInteger checkSum = new AtomicInteger(0);
        ExecutorService executor = Executors.newFixedThreadPool(threadsCount);
        CountDownLatch latch = new CountDownLatch(threadsCount);

        for(int i=0;i<threadsCount;i++) {
            executor.submit(() -> {
                try {
//                    Thread.sleep(1000);
                    System.out.println("Running on thread: " + Thread.currentThread().getName());
                    if(coordinator.deleteFile(token,department,filename)){
                        checkSum.incrementAndGet();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();  // wait for all uploads to finish
        executor.shutdown();
        System.out.println("Successful deletes: " + checkSum.get());
    }
    public void coordinatorRaceTest() throws Exception {
        ClientApp client = new ClientApp(coordinator);

        // Prepare the file data to upload
        byte[] fileData = Files.readAllBytes(Paths.get("C:/Users/Omar Fostok/Desktop/django.txt"));

        // Use the same token for all uploads (simulate race)
        String token = coordinator.login("omar2","123");

        int threadsCount = 50;
        AtomicInteger checkSum = new AtomicInteger(0);
        ExecutorService executor = Executors.newFixedThreadPool(threadsCount);
        CountDownLatch latch = new CountDownLatch(threadsCount);

        for (int i = 0; i < threadsCount; i++) {
            executor.submit(() -> {
                try {
                    // Thread.sleep(1000);
                    System.out.println("Running on thread: " + Thread.currentThread().getName());
                    if (client.uploadFile(token, "qa", Thread.currentThread().getName(), fileData)) {
                        checkSum.incrementAndGet();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }


        latch.await();  // wait for all uploads to finish
        executor.shutdown();
        System.out.println("Successful uploads: " + checkSum.get());

    }
    public void nodeRaceTest(String filename) throws Exception {

        // Prepare the file data to upload
        byte[] fileData = Files.readAllBytes(Paths.get("C:/Users/Omar Fostok/Desktop/docker.txt"));

        // Use the same token for all uploads (simulate race)
        String token = coordinator.login("omar","123");

        int threadsCount = 50;
        AtomicInteger checkSum = new AtomicInteger(0);
        ExecutorService executor = Executors.newFixedThreadPool(threadsCount);
        CountDownLatch latch = new CountDownLatch(threadsCount);


        UploadInstruction uploadInstr = (UploadInstruction) coordinator.uploadFile(token,"dev",filename);

        if(uploadInstr==null) {
            return;
        }


        for(int i=0;i<threadsCount;i++) {
            executor.submit(() -> {
                try {
//                    Thread.sleep(1000);
                    System.out.println("Running on thread: " + Thread.currentThread().getName());
                    if(uploadInstr.targetNode.upload(uploadInstr.token,fileData)) {
                        checkSum.incrementAndGet();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();  // wait for all uploads to finish
        executor.shutdown();
        System.out.println("Successful uploads: " + checkSum.get());


    }
    public static void main(String[] args) throws Exception {
        Registry registry = LocateRegistry.getRegistry("localhost", 1099);
        CoordinatorInterface coordinator = (CoordinatorInterface) registry.lookup("CoordinatorServer");
        RMIRaceTest test = new RMIRaceTest(coordinator);
//        test.nodeRaceTest("pool-1-thread-78030");
//        test.deleteRaceTest("qa","pool-1-thread-1");
        test.coordinatorRaceTest();
//        test.downloadFromNodeRaceTest("qa","pool-1-thread-2");
        System.out.println("ended successfully");

    }
}
