-- Module: System (sys)
-- Section: Role (rle)
-- Function Description: Get role list by org id
-- Params:
--  _owner_org_id
--  _include_deleted: Include deleted record
--  _include_disabled: Include disabled record
create or replace function sys_get_role_list_by_org_id(_owner_org_id bigint, _include_deleted bool, _include_disabled bool)
returns text as $$
declare 
	_query TEXT;
	ret_val TEXT;
begin
_query = format('select coalesce(json_agg(t), ''[]'')::text 
from(
	select false as "checked", id, owner_org_id as "ownerOrgId", code, name, disabled
	from role
	where (owner_org_id = %L or ( owner_org_id is null and %L is null)
 	or owner_org_id in (' || sys_get_sub_org_ids(_owner_org_id, _include_deleted, _include_disabled)  || ')
	or owner_org_id in (' || sys_get_all_parent_org_ids(_owner_org_id, _include_deleted, _include_disabled)  || '))			
	and ' || get_deleted_cond_str(null, _include_deleted) || '
	and ' || get_disabled_cond_str(null, _include_disabled) || '
	order by sort, name, created_date
) as t', _owner_org_id, _owner_org_id);

execute _query into ret_val;
return  ret_val;
end;
$$ language plpgsql called on null input;