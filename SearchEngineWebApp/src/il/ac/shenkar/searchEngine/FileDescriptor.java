package il.ac.shenkar.searchEngine;

public class FileDescriptor {
	private String title;
	private String creationDate;
	private String author;
	private String preview;
	private String path;
	private String isPic;
	private int docNum;
	private int freq;
//	private StringBuilder content;
	
	public FileDescriptor() {
		isPic = "false";
	}



	public FileDescriptor(int num, int freq) {
		this.docNum = num;
		this.freq = freq;
	}



	@Override
	public String toString() {
		return "FileDescriptor [title=" + title + ", creationDate="
				+ creationDate + ", author=" + author + ", preview=" + preview
				+ ", path=" + path + ", isPic=" + isPic + "]";
	}



	public String isPic() {
		return isPic;
	}
	public void setPic(String isPic) {
		this.isPic = isPic;
	}
	
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(String creationDate) {
		this.creationDate = creationDate;
	}



	public String getPreview() {
		return preview;
	}

	public void setPreview(String preview) {
		this.preview = preview;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}



	public int getDocNum() {
		return docNum;
	}



	public void setDocNum(int docNum) {
		this.docNum = docNum;
	}



	public int getFreq() {
		return freq;
	}



	public void setFreq(int freqLine) {
		this.freq = freqLine;
	}
}
