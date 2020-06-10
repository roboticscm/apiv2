-- Module: System (sys)
-- Section: Human or org (hoo)
-- Function Description: Get user list by org id (company id, branch id, ...)
-- Params:
--  _org_id: Owner org id
--  _include_deleted: Include deleted record
--  _include_disabled: Include disabled record
create or replace function sys_get_user_list_by_org_id(_org_id bigint, _include_deleted bool, _include_disabled bool)
returns text as $$
declare 
	_query TEXT;
	ret_val TEXT;
begin
_query = 'select coalesce(json_agg(t), ''[]'')::text 
from(
	select h.id, h.last_name as "lastName", h.first_name as "firstName", h.username, h.default_owner_org_id as "defaultOwnerOrgId", o.name as "departmentName"
	from human_or_org h
	inner join owner_org o on o.id = h.default_owner_org_id
	where h.username is not null
	and h.default_owner_org_id in (' || sys_get_sub_org_ids(_org_id, _include_deleted, _include_disabled)  || ')
	and ' || get_deleted_cond_str('h', _include_deleted) || '
	and ' || get_disabled_cond_str('h', _include_disabled) || '
	order by h.first_name, h.last_name, h.created_date
) as t';

execute _query into ret_val;
return  ret_val;
end;
$$ language plpgsql called on null input;


-- Module: System (sys)
-- Section: Human or org (hoo)
-- Function Description: Get owner org id of the user
-- Params:
--  _user_id:
-- _org_type: 10000 (company), 1000 (branch), 100 (department), 10 (group)
--  _include_deleted: Include deleted record
--  _include_disabled: Include disabled record
create or replace function sys_get_owner_id_of_user(_user_id bigint, _org_type smallint, _include_deleted bool, _include_disabled bool)
returns text as $$
declare 
	_query text;
	ret_val text;
begin
_query = format('
select array_to_string(array(
select ono.id
from owner_org ono
where ono.type = %L
	and ono.id in (' || sys_get_all_parent_org_ids((select default_owner_org_id from human_or_org where id = _user_id), _include_deleted, _include_disabled) || ')
),%L)', _org_type, ',');
execute _query into ret_val;
return  ret_val;
end;
$$ language plpgsql called on null input;


-- Module: System (sys)
-- Section: Human or org (hoo)
-- Function Description: Get last owner id of the user (company id, branch id, ...)
-- Params:
--  _org_id: Owner org id
--  _include_deleted: Include deleted record
--  _include_disabled: Include disabled record
create or replace function sys_get_last_owner_org_id_of_user(_user_id bigint, _org_type smallint)
returns text as $$
declare 
	_org_id text;
begin
	if _org_type = 10000 then
		_org_id = sys_get_value_of_user_settings(_user_id, 'system', 'companyId', 'lastCompanyId');
	elseif _org_type = 1000 then
		_org_id = sys_get_value_of_user_settings(_user_id, 'system', 'branchId', 'lastBranchId');
	elseif _org_type = 100 then
		_org_id = sys_get_value_of_user_settings(_user_id, 'system', 'departmentId', 'lastDepartmentId');
	elseif _org_type = 10 then
		_org_id = sys_get_value_of_user_settings(_user_id, 'system', 'groupId', 'lastGroupId');
	end if;
	
	if _org_id = '' then
		_org_id = sys_get_owner_id_of_user(1, _org_type, true, true);
	end if;
return _org_id;
end;
$$ language plpgsql called on null input;



create or replace function sys_get_user_info_by_id(_user_id bigint)
returns text as $$
declare 
	_query TEXT;
	ret_val TEXT;
begin
_query = format('select coalesce(json_agg(t), ''[]'')::text 
from(
	select last_name as "lastName",
		first_name as "firstName",
		username,
		font_icon as "fontIcon",
		use_font_icon as "useFontIcon",
		icon_data as "iconData"
	from human_or_org
	where id=%L
) as t', _user_id);

execute _query into ret_val;
return  ret_val;
end;
$$ language plpgsql called on null input;





-- return null if full control
-- return '' if there is no staff
-- otherwise, return list ids of staff

create or replace function get_list_staff_ids(_user_id bigint, _menu_path text, _dep_id bigint)
returns text as $$
declare 
	_query text;
	ret_val text;
	is_admin bool;
	data_level smallint;
	org_id text;
begin
-- data_level = 10000: company
-- data_level = 1000: branch
-- data_level = 100: department
-- data_level = 10: group
-- data_level = 0: default

is_admin = is_system_admin_by_user_id(_user_id, false, false);
data_level = get_max_data_level(_user_id, _menu_path, _dep_id);
org_id = sys_get_owner_id_of_user(_user_id, data_level, false, false);

IF org_id = '' THEN
	org_id = -1;
END IF;

if is_admin = true or data_level = 10000 then
	ret_val = null;
elseif data_level is not null then
	_query = format(' 
		select coalesce(array_to_string(array_agg(distinct human_id), '',''), '''')
		from human_org
		where org_id in (%s)
	', sys_get_sub_org_ids(org_id::bigint, false, false));
	execute _query into ret_val;
else 
	ret_val = '';
end if;

IF ret_val = '' THEN
	ret_val = _user_id;
END IF;

return  ret_val;
end;
$$ language plpgsql called on null input;



--version 2
CREATE OR REPLACE FUNCTION get_list_staff_ids(_user_id BIGINT, _menu_path TEXT, _dep_id BIGINT)
RETURNS TEXT AS $$
DECLARE 
	_query TEXT;
	ret_val TEXT;
	is_admin BOOL;
	data_level SMALLINT;
BEGIN
-- data_level = 10000: company
-- data_level = 1000: branch
-- data_level = 100: department
-- data_level = 10: group
-- data_level = 0: default

is_admin = is_system_admin_by_user_id(_user_id, false, false);
data_level = get_max_data_level(_user_id, _menu_path, _dep_id);


IF is_admin = TRUE OR data_level = 10000 THEN
	ret_val = null;
ELSEIF data_level IS NOT NULL THEN
	_query = format(' 
		SELECT COALESCE(ARRAY_TO_STRING(ARRAY_AGG(DISTINCT user_id), %L), %L)
		FROM assignment_role al
		WHERE al.deleted_by IS NULL AND EXISTS (
			SELECT
			FROM role_detail rd
			WHERE rd.role_id = al.role_id
				AND (rd.data_level < %L OR rd.data_level IS NULL) 
				AND EXISTS (
					SELECT 
					FROM menu_org mo
					WHERE mo.id = rd.menu_org_id
						AND mo.org_id = %L
						AND EXISTS (
							SELECT
							FROM menu
							WHERE menu.id = mo.menu_id
								AND menu.path = %L
						)
				)
		)
	', ',', '', data_level, _dep_id, _menu_path);
	execute _query into ret_val;
ELSE 
	ret_val = '';
END IF;

IF ret_val = '' THEN
	ret_val = _user_id;
END IF;

RETURN  ret_val;
END;
$$ LANGUAGE PLPGSQL CALLED ON NULL INPUT;



CREATE OR REPLACE FUNCTION find_avatars(_user_ids TEXT)
RETURNS TEXT AS $$
DECLARE 
	_query TEXT;
BEGIN


_query = format('
	SELECT id, low_icon_data as "lowIconData"
	FROM human_or_org
	WHERE low_icon_data IS NOT NULL AND id IN (%s)
', _user_ids);

RETURN json_query(_query);

END;
$$ LANGUAGE PLPGSQL CALLED ON NULL INPUT;
