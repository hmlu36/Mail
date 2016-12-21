package tw.com.softeader;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Member {
	private String name = "";

	private String email = "";

	private String path = "";

	private String group = "";

	private String size = "";

	@Override
	public String toString() {
		return "Member [name=" + name + ", email=" + email + ", path=" + path + ", group=" + group + ", size=" + size + "]";
	}

}
