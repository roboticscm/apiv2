create or replace function delete_old_rows_auth_token()
	returns trigger
    language plpgsql
    as $$
begin
  delete from auth_token where db_created_date < now() - interval '15 minute';
  return new;
end;
$$;

create trigger delete_old_rows_auth_token_trigger
    after insert on auth_token
    execute procedure delete_old_rows_auth_token();