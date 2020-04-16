-- Module: System (sys)
-- Section: Assignment Role (asr)
-- Function Description: Get all assignment role and user list
-- Params:
--  _include_deleted: Include deleted record
--  _include_disabled: Include disabled record
create or replace function sys_get_all_assignment_role_user_list(_include_deleted bool, _include_disabled bool)
returns text as $$
declare 
	_query text;
	ret_val text;
begin
_query = 'select coalesce(json_agg(t), ''[]'')::text 
from(
	select distinct ho.id, ho.last_name as "lastName", ho.first_name as "firstName", ho.username, ho.default_owner_org_id as "defaultOwnerOrgId", max(ar.created_date) as "createdDate"
	from human_or_org ho
	inner join assignment_role ar on ar.user_id = ho.id
	where 
		' || get_deleted_cond_str('ho', _include_deleted) || '
		and ' || get_disabled_cond_str('ho', _include_disabled) || '
		and ' || get_deleted_cond_str('ar', _include_deleted) || '
	group by ho.id, "lastName", "firstName", ho.username, "defaultOwnerOrgId"
	order by "createdDate" desc
) as t';

execute _query into ret_val;
return  ret_val;
end;
$$ language plpgsql called on null input;


-- Module: System (sys)
-- Section: Assignment Role (asr)
-- Function Description: Get role id list of the user
-- Params:
--  _user_id: User Id
--  _include_deleted: Include deleted record
--  _include_disabled: Include disabled record
create or replace function sys_get_role_list_of_user(_user_id bigint, _include_deleted bool, _include_disabled bool)
returns text as $$
declare 
	_query text;
	ret_val text;
begin
_query = format('select coalesce(json_agg(t), ''[]'')::text 
from(
	select role_id as "roleId"
	from assignment_role
	where user_id = %L 
		and ' || get_deleted_cond_str(null, _include_deleted) || '
		and ' || get_disabled_cond_str(null, _include_disabled) || '
) as t', _user_id);

execute _query into ret_val;
return  ret_val;
end;
$$ language plpgsql called on null input;

-- Module: System (sys)
-- Section: Assignment Role (asr)
-- Function Description: Get role ids list of the many users
-- Params:
--  _user_ids: User Ids
--  _include_deleted: Include deleted record
--  _include_disabled: Include disabled record
create or replace function sys_get_role_list_of_users(_user_ids text, _include_deleted bool, _include_disabled bool)
returns text as $$
declare 
	_query text;
	ret_val text;
begin
_query = format('select coalesce(json_agg(t), ''[]'')::text 
from(
	select user_id as "userId",  array_agg(role_id order by role_id) as "roleIds"
	from assignment_role
	where user_id in (%s)
		and ' || get_deleted_cond_str(null, _include_deleted) || '
		and ' || get_disabled_cond_str(null, _include_disabled) || '
	group by user_id
) as t', _user_ids);

execute _query into ret_val;
return  ret_val;
end;
$$ language plpgsql called on null input;
