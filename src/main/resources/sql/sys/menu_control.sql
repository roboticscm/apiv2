-- Module: System (sys)
-- Section: Menu Control (mct)
-- Function Description: Get control list by menu path
-- Params:
--  _menu_path
create or replace function sys_get_control_list_by_menu_path(_menu_path text)
returns text as $$
declare 
	_query TEXT;
	ret_val TEXT;
begin
_query = format('select coalesce(json_agg(t), ''[]'')::text 
from(
	select c.id as "controlId", c.code, c.name, case when mc.id is null then false else true end as checked
	from control c
	left join menu_control mc on mc.control_id = c.id and mc.menu_id in (select id from menu where path=%L and deleted_by is null and disabled=false) and mc.deleted_by is null and mc.disabled=false
	where c.deleted_by is null and c.disabled=false
	order by checked desc, c.sort, c.code
) as t', _menu_path);

execute _query into ret_val;
return  ret_val;
end;
$$ language plpgsql called on null input;