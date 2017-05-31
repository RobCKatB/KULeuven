package contract_net;

import com.github.rinde.rinsim.core.model.comm.CommUser;
import com.github.rinde.rinsim.core.model.comm.Message;
import com.github.rinde.rinsim.core.model.comm.MessageContents;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;

public class Mess extends Message {


	Mess() {
		super();
		// TODO Auto-generated constructor stub
	}


 public CommUser from(){};

 public Optional<CommUser> to(){};

 public MessageContents contents(){
};

public Predicate<CommUser> predicate(){};

}
