package tw.com.softeader;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Leader {
	private String group = "";
	private String cname = "";
	private String member = "";
	private String email = "";

	@Override
	public String toString() {
		return "Leader [group=" + group + ", cname=" + cname + ", member=" + member + ", email=" + email + "]";
	}

}
