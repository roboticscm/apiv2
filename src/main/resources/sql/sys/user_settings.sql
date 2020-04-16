create or replace function sys_get_value_of_user_settings(_user_id bigint, _menu_path text, _control_id text, _key text)
returns text as $$
declare 
	_query text;
	ret_val text;
begin
_query = format('
select array_to_string(array(
select value 
from user_settings
where user_id = %L
	and menu_path=%L
	and control_id=%L
	and key=%L
),%L)
', _user_id, _menu_path, _control_id, _key, ',');
execute _query into ret_val;
return  ret_val;
end;
$$ language plpgsql called on null input;



create or replace function sys_get_user_settings(_user_id bigint, _com_id bigint)
returns text as $$
declare 
	_company_id text;
	_department_id text;
	_menu_path text;
	_lang text;
	_theme text;
	_alpha text;
	_header_height text;
begin
	_company_id = sys_get_value_of_user_settings(_user_id, 'system', 'companyId', 'lastCompanyId');
	_department_id = sys_get_last_department_id_by_user_id(_user_id, _com_id);
	_menu_path = sys_get_value_of_user_settings(_user_id, 'system', 'mainNavBarId', 'lastMenuPath');
	_lang = sys_get_value_of_user_settings(_user_id, 'system', 'localeResourceId', 'lastLocaleResource');
	_theme = sys_get_value_of_user_settings(_user_id, 'sys/theme', 'themeId', 'lastTheme');
	_alpha = sys_get_value_of_user_settings(_user_id, 'sys/theme', 'themeId', 'lastAlpha');
	_header_height = sys_get_value_of_user_settings(_user_id, 'sys/main-layout', 'mainLayoutId', 'lastHeaderHeight');
	
	if _company_id = '' then
		_company_id = sys_get_owner_id_of_user(_user_id, 10000::smallint, false, false);
	end if;
	
	if _menu_path = '' and _department_id != '' then
		_menu_path = sys_get_first_roled_menu_path_by_user_id_and_dep_id(_user_id, _department_id::bigint,  false, false);
	elseif sys_is_menu_path_assigned_for_user(_menu_path, _user_id, false, false) = false then
		_menu_path = '';
	end if;
	
	if _lang = '' then
		_lang='vi-VN';
	end if;
	
	if _theme = '' then
		_theme = 'theme-green';
	end if;
	
	if _alpha = '' then
		_alpha = '1';
	end if;
	
	if _header_height = '' then
		_header_height = '50px';
	end if;
	
	
-- 	column must be in order bellow
-- 	companyId,
-- 	depId,
-- 	menuPath,
-- 	lang,
-- 	theme,
-- 	alpha,
-- 	headerHeight,
return _company_id || '#' || _department_id || '#' || _menu_path || '#' || _lang || '#' || _theme || '#' || _alpha || '#' || _header_height || '#' ;
end;
$$ language plpgsql called on null input;




create or replace function sys_get_last_department_id_by_user_id(_user_id bigint, _company_id bigint)
returns text as $$
declare 
	_department_id text;
begin
	if is_system_admin_by_user_id(_user_id, false, false) then
		_department_id = sys_get_value_of_user_settings(_user_id, 'system', 'moduleId', 'lastDepartmentId');
		if _department_id = '' then
			_department_id = sys_get_first_department_id_by_company_id(_company_id);
		end if;
	else 
		_department_id = sys_get_value_of_user_settings(_user_id, 'system', 'moduleId', 'lastDepartmentId');
		if _department_id = '' then
			_department_id = sys_get_first_roled_department_id_by_user_id(_user_id, false, false);
		elseif sys_is_department_assigned_for_user(_department_id::bigint, _user_id, false, false) = false then
			_department_id = '';
		end if;

		if _department_id = '' then
			_department_id = '-1';
		end if;
	end if;
return _department_id ;
end;
$$ language plpgsql called on null input;