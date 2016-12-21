package tw.com.softeader;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Group {

	private List<Leader> leaders = new ArrayList<Leader>();
	private List<Member> members = new ArrayList<Member>();

	@Override
	public String toString() {
		return "Group [leaders=" + leaders + ", members=" + members + "]";
	}

}
