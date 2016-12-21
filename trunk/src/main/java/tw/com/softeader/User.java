package tw.com.softeader;


public class User {
	String name = "";
	
	String email = "";
	
	String path = "";

	@Override
	public String toString() {
		return "User [name=" + name + ", email=" + email + ", path=" + path + "]";
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
	
	
}
