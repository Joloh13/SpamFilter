public class Filter {

	ArrayList<String> keywords = new ArrayList<String>();
	ArrayList<String> offenders = new ArrayList<String>();
	ArrayList<Integer> MINS = new ArrayList<Integer>();
	BufferedReader in;
	boolean isSpam = false;
	Message[] emails;
	Message msg;
	String blacklist;
	String keyword_list;
	String mails;
		
	// Reads happen first
	public void readKeywords(String k) throws FileNotFoundException,
			IOException {
		in = new BufferedReader(new FileReader(k));
		String line = in.readLine();
		while ((line == null) == false) {
			keywords.add(line);
			line = in.readLine();
		}
		in.close();
	}

	public void readBlackList(String b) throws FileNotFoundException,
			IOException {
		in = new BufferedReader(new FileReader(b));
		String line = in.readLine();
		while ((line == null) == false) {
			offenders.add(line);
			line = in.readLine();
		}
		in.close();
	}

	public void countEmails(String letters) throws FileNotFoundException,
			IOException {
		in = new BufferedReader(new FileReader(letters));
		int no_of_mails = 0;
		String line = in.readLine();
		while ((line == null) == false) {
			if (line.equals("<BEGIN>")) {
				no_of_mails++;
			}
			line = in.readLine();
		}
		in.close();
		System.out.println("No of emails found = " + no_of_mails);
		createEmail(no_of_mails);
	}

	public void createEmail(int n) throws FileNotFoundException, IOException {
		emails = new Message[n];
		BufferedReader buff = new BufferedReader(new FileReader("mail.txt"));
		PrintWriter out;

		for (int i = 0; i < n; i++) {
			String filename = "msg" + (i + 1) + ".txt";
			out = new PrintWriter(new BufferedWriter(new FileWriter(filename)));
			String read_line = buff.readLine();
			do {
				out.println(read_line);
				read_line = buff.readLine();
				if (read_line == null)
					break;
			} while ((read_line.equals("<BEGIN>")) == false);
			out.close();
			emails[i] = new Message(filename);
		}
		buff.close();
	}

	// Now we create a filter. So constructor
	public Filter(String black_list, String keywords, String emails)
			throws FileNotFoundException, IOException {
		blacklist = black_list;
		keyword_list = keywords;
		mails = emails;
		readBlackList(blacklist);
		readKeywords(keyword_list);
		countEmails(mails);
	}

	// Now we check for Spam
	public void spamIdentifier() throws FileNotFoundException, IOException {
		for (int k = 0; k < emails.length; k++) {
			msg = emails[k];
			msg.spam = senderTest();
		}
		System.out.println("Spam filtering complete.");
	}

	public boolean alreadyExists(String x, ArrayList<String> p) {
		for (int i = 0; i < p.size(); i++) {
			if (p.get(i).equalsIgnoreCase(x)){
				return true;
			}
		}
		return false;
	}

	// Tests go here
	public boolean senderTest() throws FileNotFoundException, IOException {
		if (alreadyExists(msg.sender, offenders)) {
			System.out.println("Debug: We have found an existing offender!");
			addMIN(msg.MIN);
			System.out.println("Debug: Added MIN from senderTest()");
			
			String[] words = msg.subjectLine.split(" ");
			
			for (int x = 0; x < words.length; x++) {
				System.out.println("Debug: Currently processing word: "+words[x]);
				if ((alreadyExists(words[x], keywords) == false) && (words[x].length() > 6)) {
						System.out.println("Debug: "+words[x]+" does not exist in the list and is a valid candidate" );
						addKeywords(words[x]);
						System.out.println("Debug: Keywords is now "+keywords.toString());
					}
				}
			
			return true;
			
			}else{
			System.out.println("Debug: About to access subjectLine Test");
			return subjectLineTest();
		}
	}

	public boolean subjectLineTest() throws FileNotFoundException, IOException {
		System.out.println("Debug: AM in subject Line test now!");
		
		String[] words = msg.subjectLine.split(" ");
		for (int j = 0; j < words.length; j++) {
			if (alreadyExists(words[j], keywords)) {
				System.out.println("Debug: "+words[j]+" appears to already exist in "+keywords.toString());
				addMIN(msg.MIN);
				System.out.println("Debug: Added MIN "+msg.MIN+"to output");
				addSender(msg.sender);
				System.out.println("Debug: Offenders is now: "+offenders.toString());
				if (words[j].length() > 6){
					System.out.println("Debug: Added to keywords any other wordss we come across");
					addKeywords(words[j]);
					System.out.println("Debug: Keywords is now: "+keywords.toString());	
				}
				return true;
			}
		}
		return BodyTest();
	}

	public boolean BodyTest() throws FileNotFoundException, IOException {
		System.out.println("Debug: Am testing the body now!");
		String words[] = msg.subjectLine.split(" ");
		for (int a = 0; a < msg.body.size(); a++) {
			if (alreadyExists(msg.body.get(a), keywords)) {
				System.out.println("Debug: "+words[a]+" appears to already exist in "+keywords.toString());
				addMIN(msg.MIN);
				System.out.println("Debug: Added MIN "+msg.MIN+"to output");
				addSender(msg.sender);
				System.out.println("Debug: Offenders is now: "+offenders.toString());
				for (int x = 0; x < words.length; x++) {
					if (msg.subjectLine.length() > 6){
						System.out.println("Debug: Added to keywords any other wordss we come across");
						addKeywords(words[x]);
						System.out.println("Debug: Keywords is now: "+keywords.toString());
					}
				}
				return true;
			}
		}
		return false;
	}

	// Additions to files happens here
	public void addSender(String sender) throws FileNotFoundException,
			IOException {
		System.out.println("Debug: About to add the offender! Die, "+sender);
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(
				blacklist, true)));
		out.println(sender);
		out.close();
	}

	public void addMIN(int min) throws FileNotFoundException, IOException {
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(
				"output.txt", true)));
		out.println(min);
		out.close();
	}

	public void addKeywords(String keyword) throws FileNotFoundException,
			IOException {
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(
				keyword_list, true)));
		out.println(keyword);
		out.close();
	}
}
