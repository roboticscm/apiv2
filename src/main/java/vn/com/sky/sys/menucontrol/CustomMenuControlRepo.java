package vn.com.sky.sys.menucontrol;

import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import vn.com.sky.base.BaseR2dbcRepository;

@Repository
public class CustomMenuControlRepo extends BaseR2dbcRepository {

    /*
	-- Module: System (sys)
	-- Section: Menu Control (mct)
	-- Function Description: Get All control list by menu path
	-- Params:
	--  menuPath

	*/
    public Mono<String> sysGetControlListByMenuPath(String menuPath) {
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();

        var ret = this.databaseClient().execute(genSql(methodName, "menuPath")).bind("menuPath", menuPath);

        return ret.as(String.class).fetch().first();
    }
}
