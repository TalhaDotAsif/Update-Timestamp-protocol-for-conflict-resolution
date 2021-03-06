import java.util.*;
import java.util.concurrent.TimeUnit;
import java.io.IOException;
import java.lang.*;
import java.net.*;

public class Node2 extends Thread{
private String Node2Number;
private List<String> neighbours; 
private List<Integer> neighboursPorts;
private List<String> seqNumber;
private int[] coordinates;
private int serverPort;
private int fport;
private InetAddress ip;
private DatagramSocket sock;
private DatagramPacket sendpacket;
private DatagramPacket recvpacket;
private byte recvBuffer[];
private byte sendBuffer[];
private String fptr;
private String rptr;
private static boolean DSR=false;
private static boolean path=false;
private static boolean backtrack=false;
public static boolean low=false;

Node2(int num) throws IOException{		//Node2 number is passed as argument
Node2Number="#"+num;
neighbours=new ArrayList<String>();
neighboursPorts=new ArrayList<Integer>();
seqNumber=new ArrayList<String>();
coordinates=new int[2];

//Random rand=new Random();    

coordinates[0]=0;		//x coordinate	with range 0-300	
coordinates[1]=num;
if(num>20)
	{coordinates[0]++;
coordinates[1]=num-20;		//y coordinate with range 0-300
	}
//coordinates[0]=rand.nextInt(301);		//x coordinate	with range 0-300	
//coordinates[1]=rand.nextInt(301);		//y coordinate with range 0-300
//System.out.println(Node2Number+" "+coordinates[0]+" "+coordinates[1]+"\n" );

serverPort=setPort();
sendpacket=null;
recvpacket=null;
recvBuffer=null;
sendBuffer=null;
fptr="";
rptr="";


try {
	
	sock=new DatagramSocket(serverPort);		//Node2 server port number 
	ip=InetAddress.getLocalHost();
	
} catch (UnknownHostException e) {
	// TODO Auto-generated catch block
	e.printStackTrace();
}
}

protected void finalize() 
{
	sock.close();
}

protected void setDSRtrue()
{
DSR=true;	
}

protected void setDSRfalse()
{
DSR=false;	
}

public void clear()
{
this.rptr="";
this.fptr="";
}


protected void mobility() {			//mobility function with range of 0-5
    Random r = new Random();
    this.coordinates[0]=0;
    //this.coordinates[0]+=r.nextInt(6) + 1;		
    this.coordinates[1]+=r.nextInt(6) + 1;
 //   System.out.println(this.Node2Number+" "+this.coordinates[0]+" "+this.coordinates[1]+"\n" );
}


private int getDistance(Node2 obj)
{
	int x=this.coordinates[0]-obj.coordinates[0];
	int y=this.coordinates[1]-obj.coordinates[1];
	x=(int) Math.pow(x, 2);
	y=(int) Math.pow(y, 2);
	x+=y;
	return (int) Math.pow(x, 0.5);
}

protected void printNeighbours()
{
	if(!this.neighbours.isEmpty())
		{
		System.out.println("current Node2 number :"+this.Node2Number+" "+this.getPort());

			for(int j=0;j<this.neighbours.size();j++)
				System.out.print(this.neighbours.get(j)+" ");
			System.out.println();
			for(int j=0;j<this.neighboursPorts.size();j++)
				System.out.print(this.neighboursPorts.get(j)+" ");
			System.out.println();

		}
}

void addNeighbour(Node2[] arr)
{
if(!this.neighbours.isEmpty())		//checking current neighbours and dropping them if out of range	
	{
	for(int j=0;j<this.neighbours.size();j++)
		{
		for(int k=0;k<arr.length;k++)
		{
			if(!arr[k].neighbours.isEmpty() && this.neighbours.get(j).equals(arr[k].Node2Number))
			{
			
				if(arr[k].getDistance(this)>1)
				{
					this.neighbours.remove(j);
					arr[k].neighbours.remove(this.Node2Number);
					if(this.neighbours.size()==j)
						break;
				}
			
			}	
		}
		
		}
	}
for(int i=0;i<arr.length;i++)
	{
	if(arr[i].Node2Number!=this.Node2Number )
		{
		if(arr[i].getDistance(this)<2 && !this.neighbours.contains(arr[i].Node2Number))
			{
			this.neighbours.add(arr[i].Node2Number);
			arr[i].neighbours.add(this.Node2Number);
			
			}
		}
	}

}

void populatePorts(Node2[] arr)
{
this.neighboursPorts=new ArrayList<Integer>();	
for(int i=0;i<arr.length;i++)
	if(this.neighbours.contains(arr[i].Node2Number))
		this.neighboursPorts.add(arr[i].getPort());
	
return;
}

public String getNode2number()
{
return this.Node2Number;	
}

private int setPort() throws IOException
{
ServerSocket obj=new ServerSocket(0);
int x=obj.getLocalPort();
obj.close();
return x;
}

private int getSeqNo()
{
Random rand=new Random();
return rand.nextInt(5001);		//range is 0-5000
}

public void sendMessage(Node2[] arr,String rNo,String sNo, String m) throws IOException, InterruptedException
{
//System.out.println("in sndmsg ");	
	
String recvNumber=rNo;
String sendNumber=sNo;
String message=m;

String t="#";
t+=message;
message=t;

boolean check=false;
int index=0;
for(int i=0;i<arr.length && !check;i++)
	if(arr[i].Node2Number.equals(sendNumber))
		{
		check=true;
		index=i;
		}


int temp=this.getSeqNo();
String seqNo="#"+temp;
//arr[index].seqNumber.add(seqNo);
seqNo+=recvNumber;
seqNo+=message;
message=seqNo;
message+=arr[index].Node2Number;
arr[index].sendFlooding(message);
}

int getPort()
{
return this.serverPort;	
}

public void run()
{
	while(true)
	{
	this.recvBuffer=new byte[1024];
	this.recvpacket=new DatagramPacket(this.recvBuffer,this.recvBuffer.length);
	try {
		this.sock.receive(recvpacket);
		//this.recvrport=recvpacket.getPort();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	String message=new String(recvpacket.getData());
	try {
		if(message.charAt(0)=='#')
		{
			Random rand=new Random();
			int drop=30;//rand.nextInt(101);		//min=1 ,max=100
			if(drop!=1 || drop>20)
			this.sendFlooding(message);
			else
				{
				System.out.println("Packet has been dropped at Node2 : "+this.Node2Number);
				}
			}
	} catch (IOException | InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
	this.recvpacket=null;
	}
}

void listenFlooding() throws IOException			//threaded server for handling multiple messages at once
{
	Thread t=new Thread(this);
	t.start();
}

void setHigh()
{
low=false;	
}

void setLow()
{
low=true;	
}


synchronized void sendFlooding(String msg) throws IOException, InterruptedException
{
	String seqNo="#";
	int i=1;
	if(i<msg.length())
	for(;msg.charAt(i)!='#' && i<msg.length();i++)
		seqNo+=msg.charAt(i);
	
	String recvNo="#";
	i++;
	if(i<msg.length())
	for(;msg.charAt(i)!='#'&& i<msg.length();i++)
		recvNo+=msg.charAt(i);
	
	if(this.seqNumber.contains(seqNo))
			return;
	this.seqNumber.add(seqNo);
	if(this.neighbours.isEmpty())
		return;
	
	String temp="";
	i++;
	if(i<msg.length())
	for(;msg.charAt(i)!='#' && i<msg.length();i++)
		temp+=msg.charAt(i);
	
	String sender="";
	if(i<msg.length())
	for(;i<msg.length();i++)
		sender+=msg.charAt(i);
	
	if(path && !this.Node2Number.equals(recvNo))
	{
		for(int k=0;k<this.neighbours.size();k++)
		{
			if(this.fptr.equals(this.neighbours.get(k)))
			{
				int portNo=this.neighboursPorts.get(k);
				this.sendBuffer=new byte[1024];
				this.sendBuffer=msg.getBytes();
				this.sendpacket=new DatagramPacket(sendBuffer,sendBuffer.length,this.ip,portNo);
				sock.send(sendpacket);
				break;
			}
		}
		return;
	}

	
	if(path && recvNo.equals(this.Node2Number))
	{
		System.out.println("RECIEVED DSR MESSAGE");
		System.out.println(msg);
		System.out.println(this.Node2Number);
	return;
	}
	
	if(temp.equals("route request"))		//populating backtracking Node2s
	{
		if(this.recvpacket!=null && !this.Node2Number.equals(sender))
		{
		int port=this.recvpacket.getPort();
		for(int k=0;k<this.neighboursPorts.size();k++)
			if(port==this.neighboursPorts.get(k))
			{
				this.rptr=this.neighbours.get(k);
			}
		}
	}
	
	if(temp.equals("route reply"))
	{		
		if(this.rptr.equals(""))		//for sender when it receives the route reply
			{
				int port=this.recvpacket.getPort();
				for(int k=0;k<this.neighboursPorts.size();k++)
					if(port==this.neighboursPorts.get(k))
					{
					//	System.out.println("BACK IN SENDER!!!!!");
						this.fptr=this.neighbours.get(k);
						
						path=true;//acquired desired path
				//	System.out.println("Node2 number is : "+this.Node2Number);
					System.out.println("route reply recieved "+msg);
						break;
					}
				return;
			}
		else
		for(int j=0;j<this.neighbours.size();j++)
		if(this.rptr.equals(this.neighbours.get(j)))
		{				
			String here="set fptr";
			int portNo=this.neighboursPorts.get(j);
			this.sendBuffer=new byte[1024];
			this.sendBuffer=here.getBytes();
			this.sendpacket=new DatagramPacket(sendBuffer,sendBuffer.length,this.ip,portNo);
			sock.send(sendpacket);
			
			//if(low)
			//TimeUnit.SECONDS.sleep(1);
			portNo=this.neighboursPorts.get(j);
			this.sendBuffer=new byte[1024];
			this.sendBuffer=msg.getBytes();
			this.sendpacket=new DatagramPacket(sendBuffer,sendBuffer.length,this.ip,portNo);
			sock.send(sendpacket);
			//if(low)
			//TimeUnit.SECONDS.sleep(1);
			
			int port=this.recvpacket.getPort();
			for(int k=0;k<this.neighboursPorts.size();k++)
				if(port==this.neighboursPorts.get(k))
					this.fptr=this.neighbours.get(k);
			//System.out.println("Node2 NUMBER : "+this.Node2Number);
			//System.out.println("TO RETURN RREP : "+this.rptr+" "+" port "+this.neighboursPorts.get(j));			
			//System.out.println("TO FORWARD MSG : "+this.fptr+" "+" port "+this.recvpacket.getPort());
			
			break;
		}
		
	}
	
	
	if(recvNo.equals(this.Node2Number))
	{
		System.out.println("Got the Message");
		System.out.println(msg);
		System.out.println(this.Node2Number);
		if(DSR)
		{
			if(temp.equals("route request"))
			{
				String back="";
				back+="#"+this.getSeqNo();
				back+=sender;
				back+="#route reply";
				back+=this.Node2Number;
				backtrack=true;
				System.out.println("ON MY WAY BACK");
				//System.out.println("Back MSG "+back);
				this.sendFlooding(back);
				
			}
			
			if(temp.equals("route reply"))
			{
				backtrack=false;
			}
		}
		return;
	}


	if(DSR && !path && temp.equals("route request"))	//for route request
	for(int j=0;j<this.neighboursPorts.size();j++)
		{
		int portNo=this.neighboursPorts.get(j);
		this.sendBuffer=new byte[1024];
		this.sendBuffer=msg.getBytes();
		this.sendpacket=new DatagramPacket(sendBuffer,sendBuffer.length,this.ip,portNo);
		sock.send(sendpacket);
		}
	
		
	if(!DSR && !path)	//for simple flooding
	for(int j=0;j<this.neighboursPorts.size();j++)
		{
		int portNo=this.neighboursPorts.get(j);
		this.sendBuffer=new byte[1024];
		this.sendBuffer=msg.getBytes();
		this.sendpacket=new DatagramPacket(sendBuffer,sendBuffer.length,this.ip,portNo);
		sock.send(sendpacket);
		}
		
	
	}	

synchronized void Dsr(String msg,String rNo,String sNo,Node2[] arr) throws IOException, InterruptedException
{
	System.out.println("Sno "+ sNo);
	System.out.println("Rno "+ rNo);
	this.DSR=true;
	
	String temp="route request";
	this.sendMessage(arr, rNo, sNo, temp);
	TimeUnit.SECONDS.sleep(1);
	String temp2="route reply";
	TimeUnit.SECONDS.sleep(1);
	this.sendMessage(arr, rNo, sNo, msg);
	this.DSR=false;	
	this.path=false;
	this.backtrack=false;
	for(int i=0;i<arr.length;i++)
		arr[i].clear();
	TimeUnit.SECONDS.sleep(1);
}
	
}
