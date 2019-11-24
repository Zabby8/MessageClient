package ua.kiev.prog;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

public class Message implements Serializable {

	private Date date = new Date();
	private String from;
	private String to;
	private String text;
	private boolean Private;

	public String toJSON(){
		Gson gson = new GsonBuilder().create();
		return gson.toJson(this);
	}

	public static Message fromJSON(String s){
		Gson gson = new GsonBuilder().create();
		Message message = gson.fromJson(s, Message.class);
		return message;
	}

	@Override
	public String toString() {
		return new StringBuilder().append("[").append(date.toString())
				.append(", From: ").append(from).append(", To: ").append(to)
				.append("] ").append(text).toString();
	}

	public int send(String url) throws IOException {
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		con.setRequestMethod("POST");
		con.setDoOutput(true);

		try (OutputStream os = con.getOutputStream()){
			String json = toJSON();
			os.write(json.getBytes());
			return con.getResponseCode();
		}
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public boolean getPrivate() {
		return Private;
	}

	public void setPrivate(boolean aPrivate) {
		Private = aPrivate;
	}


	/*	public void writeToStream(OutputStream out) throws IOException {
		ByteArrayOutputStream bs = new ByteArrayOutputStream();
		ObjectOutputStream os = new ObjectOutputStream(bs);
		try {
			os.writeObject(this);
		} finally {
			os.flush();
			os.close();
		}

		byte[] packet = bs.toByteArray();

		DataOutputStream ds = new DataOutputStream(out);
		ds.writeInt(packet.length);
		ds.write(packet);
		ds.flush();
	}

	public static Message readFromStream(InputStream in) throws IOException,
			ClassNotFoundException {
		if (in.available() <= 0)
			return null;

		DataInputStream ds = new DataInputStream(in);
		int len = ds.readInt();
		byte[] packet = new byte[len];
		ds.read(packet);

		ByteArrayInputStream bs = new ByteArrayInputStream(packet);
		ObjectInputStream os = new ObjectInputStream(bs);
		try {
			Message msg = (Message) os.readObject();
			return msg;
		} finally {
			os.close();
		}
	}*/
}
