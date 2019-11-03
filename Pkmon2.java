import java.lang.reflect.Array;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.Thread.sleep;

class Producer extends Thread {
    private Buffer _buf;

    public Producer(Buffer buf){
        this._buf=buf;
    }
    public void run() {
        for (int i = 0; i < 10; i++) {
            _buf.put(i);
        }
    }
}

class Consumer extends Thread {
    private Buffer _buf;

    public Consumer(Buffer buf) {
        this._buf = buf;
    }

    public void run() {
        for (int i = 0; i < 10; i++) {
            _buf.get();
        }
    }
}

class Process extends Thread {
    private int number;
    private Buffer _buf;

    public Process(Buffer buf, int number){
        this._buf=buf;
        this.number=number;
    }

    public void run(){
        for (int i = 0; i < 10; i++) {

            _buf.change(number);
        }
    }

}

class Buffer {
    private int [] buffer;
    private int maxSize;
    private int[] processes;
    private int numberProcess;
    private boolean isFull;
    private int producer;
    private int consumer;

    public Buffer(int size, int number){
        this.buffer=new int [size];
        this.maxSize=size;
        this.processes=new int[number];

        this.numberProcess=number;
        this.isFull=false;
        this.producer=0;
        this.consumer=0;
        for(int i=0; i<number;i++){
            processes[i]=0;
        }
    }

    public synchronized void put(int i) {
        while(producer==maxSize) {
            isFull=true;
            producer=0;
        }
        while(consumer==maxSize){
            isFull=false;
            consumer=0;
        }

        while(isFull&& producer==consumer) {
            try{
                wait();
            }catch (InterruptedException e) {
                System.out.println(e);
            }
        }

        buffer[producer]=i;
        producer++;
        System.out.println("Producer " + i);
        notifyAll();
    }


    public synchronized void change(int n) {
        while(processes[n]==maxSize-1) {
            processes[n]=0;
        }
        while(n-1>=0) {
            while (processes[n]- processes[n-1]==0) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    System.out.println(e);
                }
            }
        }
        while(n==0 && producer==processes[n]){
            try{
                wait();
            }catch (InterruptedException e){
                System.out.println(e);
            }
        }

        int index=processes[n];
        buffer[index]++;

        System.out.println("Process "+ n + " inclement on index "+ index +", buffer value: " + buffer[index]);
        processes[n]++;
        notifyAll();
    }

    public synchronized void get(){
        while(consumer-processes[numberProcess-1]==0){
            try{
                wait();
            }catch (InterruptedException e){
                System.out.println(e);
            }
        }
        while(consumer==maxSize){
            isFull=false;
            consumer=0;
        }
        int val= buffer[consumer];
        consumer++;
        System.out.println("Consumer get value: " + val);
        notifyAll();
    }

}

public class Pkmon2 {
    public static void main(String[] args) {
        int number = 5;
        int size=100;
        Buffer buf = new Buffer(size,number);

        Producer producer = new Producer(buf);
        producer.start();

        Process[] processesThread = new Process[number];

        for(int i=0; i<number; i++) {
            processesThread[i] = new Process(buf, i);
            processesThread[i].start();
        }

        Consumer consumer = new Consumer(buf);
        consumer.start();

    }
}