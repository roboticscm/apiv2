package vn.com.sky.sys.localeresource;

import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import vn.com.sky.base.BaseR2dbcRepository;

@Repository
public class CustomLocaleResourceRepo extends BaseR2dbcRepository {

    /*
	-- Module: System (sys)
	-- Section: Locale Resource (res)
	-- Function Description: Get Locale Resource list by company id and locale
	-- Params:
	--  companyId
	--  locale
	--  includeDeleted: Include deleted record
	--  includeDisabled: Include disabled record
	*/
    public Mono<String> sysGetLocaleResourceListByCompanyIdAndLocale(
        Long companyId,
        String locale,
        Boolean includeDeleted,
        Boolean includeDisabled
    ) {
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();

        var ret =
            this.databaseClient()
                .execute(genSql(methodName, "companyId", "locale", "includeDeleted", "includeDisabled"))
                .bind("companyId", companyId)
                .bind("locale", locale)
                .bind("includeDeleted", includeDeleted)
                .bind("includeDisabled", includeDisabled);

        return ret.as(String.class).fetch().first();
    }

    public Mono<String> sysGetUsedLanguages() {
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();

        var ret = this.databaseClient().execute(genSql(methodName));

        return ret.as(String.class).fetch().first();
    }

    public Mono<String> sysGetUsedLangCategories(String textSearch) {
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();

        var ret = this.databaseClient().execute(genSql(methodName, "textSearch"));

        if (textSearch != null) ret = ret.bind("textSearch", textSearch); else ret =
            ret.bindNull("textSearch", String.class);

        return ret.as(String.class).fetch().first();
    }

    public Mono<String> sysGetUsedLangTypeGroups(String textSearch) {
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();

        var ret = this.databaseClient().execute(genSql(methodName, "textSearch"));

        if (textSearch != null) ret = ret.bind("textSearch", textSearch); else ret =
            ret.bindNull("textSearch", String.class);

        return ret.as(String.class).fetch().first();
    }

    public Mono<String> sysGetAllLanguages(Boolean includeDeleted, Boolean includeDisabled) {
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();

        var ret =
            this.databaseClient()
                .execute(genSql(methodName, "includeDeleted", "includeDisabled"))
                .bind("includeDeleted", includeDeleted)
                .bind("includeDisabled", includeDisabled);

        return ret.as(String.class).fetch().first();
    }

    /*
	-- Module: System (sys)
	-- Section: Locale Resource (res)
	-- Function Description: Get Locale Resource list by company id, category, type group and search text on key or value
	-- Params:
	--  _company_id
	--  _category
	--  _type_group
	--  _text_search
	*/

    public Mono<String> sysGetLocaleResourceByCompanyIdAndCatAndTypeGroup(
        long companyId,
        String category,
        String typeGroup,
        String textSearch,
        Long page,
        Long pageSize
    ) {
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();

        System.out.println(page);
        System.out.println(pageSize);
        var ret =
            this.databaseClient()
                .execute(genSql(methodName, "companyId", "category", "typeGroup", "textSearch", "page", "pageSize"))
                .bind("companyId", companyId)
                .bind("page", page)
                .bind("pageSize", pageSize);

        if (category == null) ret = ret.bindNull("category", String.class); else ret = ret.bind("category", category);

        if (typeGroup == null) ret = ret.bindNull("typeGroup", String.class); else ret =
            ret.bind("typeGroup", typeGroup);

        if (textSearch == null) ret = ret.bindNull("textSearch", String.class); else ret =
            ret.bind("textSearch", textSearch);

        return ret.as(String.class).fetch().first();
    }
}
