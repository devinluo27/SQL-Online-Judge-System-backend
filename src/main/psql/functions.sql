select * from assignment;

alter sequence assignment_assignment_id_seq restart with 100;
alter sequence question_question_id_seq restart with 100;
alter sequence question_trigger_id_seq restart with 100;
alter sequence database_database_id_seq restart with 100;
alter sequence database_id_seq restart with 100;
alter sequence record_record_id_seq restart with 10000;
alter sequence verify_code_cid_seq restart with 110;
alter sequence user_files_id_seq restart with 110;
alter sequence sys_permission_id_seq restart with 100;
alter sequence sys_request_path_id_seq restart with 100;
alter sequence sys_request_path_permission_relation_id_seq restart with 100;
alter sequence sys_role_id_seq restart with 100;
alter sequence sys_role_permission_relation_id_seq restart with 100;
alter sequence sys_user_role_relation_id_seq restart with 100;
alter sequence sql_type_id_seq restart with 100;


truncate user_db cascade;
truncate sys_user_role_relation cascade;
truncate verify_code;

select sys_request_path.id, url, permission_id, permission_code, permission_name
from sys_request_path left outer join
    sys_request_path_permission_relation
        on sys_request_path.id = sys_request_path_permission_relation.url_id
    join sys_permission on permission_id = sys_permission.id
order by url;



select add_url('/admin/files/createDatabase', 8);
select add_url('/admin/queryRemoteFileList', 15);
select add_url('/admin/deleteRemoteFileById', 15);



select add_url('/user/loginCountToday', 13);
select add_url('/user/getRecordCountForAWeek', 14);
select add_url('/admin/checkSimilarityByQid', 15);
select add_url('/user/getLeaderBoardByQid', 13);


select add_url('/admin/deletePermissionOfRole', 14);
select add_url('/admin/addPermissionToRole', 14);
select add_url('/admin/addUserToNewRole', 14);
select add_url('/admin/deleteUserRole', 14);



create or replace function check_question() returns trigger
    language plpgsql
as
$$
begin
    if (new.operation_type = 'trigger') then
        raise exception 'haha';
    end if;
    -- todo: implement
--     insert into question_trigger (question_id, ans_table_path, test_data_path, test_config, target_table)
--     VALUES (question_id, new.ans_table_path, new.test_data_path, new.test_config, new.target_table);
    return new;
end
$$;


create or replace function delete_question_trigger() returns trigger
    language plpgsql
as
$$
begin
    if (old.operation_type = 'trigger') then
        delete
        from question_trigger
        where question_id = old.question_id;
    end if;
    return null;
end
$$;

create or replace function add_url(new_url character varying, permission_code integer) returns void
    language plpgsql
as
$$
declare
    new_url_id int;
begin
    insert into sys_request_path (url)
    values (new_url);
    SELECT max(id) from sys_request_path into new_url_id;
    if (new_url like '/user/%') then
        insert into sys_request_path_permission_relation(url_id, permission_id)
        values (new_url_id, 13);
        return;
    end if;
    insert into sys_request_path_permission_relation(url_id, permission_id)
    values (new_url_id, permission_code);
end
$$;

create or replace function update_question_trigger() returns trigger
    language plpgsql
as
$$
begin
    if (new.operation_type = 'trigger') then
        update question_trigger qt
        set ans_table_file_id = cast(TG_ARGV[0] as int),
            test_data_file_id = cast(TG_ARGV[1] as int),
            target_table      = cast(TG_ARGV[2] as int),
            test_config       = cast(TG_ARGV[3] as int)
        where qt.question_id = new.question_id;
    end if;
end
$$;

create or replace function update_question_trigger(input_question_id integer, input_ans_table_file_id integer,
                                                   input_test_data_file_id integer,
                                                   input_target_table character varying,
                                                   input_test_config integer) returns void
    language plpgsql
as
$$
begin
    update question_trigger qt
    set ans_table_file_id = cast(input_ans_table_file_id as int),
        test_data_file_id = cast(input_test_data_file_id as int),
        target_table      = input_target_table,
        test_config       = cast(input_test_config as int)
    where qt.question_id = input_question_id;
end
$$;

create or replace function add_permission_to_role(input_role_code character varying,
                                                  input_permission_code character varying) returns void
    language plpgsql
as
$$
declare
    my_role_id       int;
    my_permission_id int;
begin
    select sr.id as role_id, sp.id as permission_id
    from sys_role sr
             cross join sys_permission sp
    where sr.role_code = input_role_code
      and sp.permission_code = input_permission_code
    into my_role_id, my_permission_id;

    insert into sys_role_permission_relation (role_id, permission_id)
    values (my_role_id, my_permission_id)
    on conflict do nothing;
end;
$$;

create or replace function delete_permission_of_role(input_role_code character varying,
                                                     input_permission_code character varying) returns void
    language plpgsql
as
$$
declare
    my_role_id       int;
    my_permission_id int;
begin
    select sr.id as role_id, sp.id as permission_id
    from sys_role sr
             cross join sys_permission sp
    where sr.role_code = input_role_code
      and sp.permission_code = input_permission_code
    into my_role_id, my_permission_id;

    delete
    from sys_role_permission_relation
    where role_id = my_role_id
      and permission_id = my_permission_id;
end;
$$;

create or replace function get_today_user_login_count() returns integer
    language plpgsql
as
$$
declare
    my_count int := 0;
begin
    select count(*) user_login_count
    from (select sid
          from user_db u
          where u.last_login_time::date = current_date - 1) temp_table
    into my_count;
    return my_count;
end;
$$;

create or replace function add_new_role_to_user(input_sid integer, input_role_code character varying) returns void
    language plpgsql
as
$$
declare
    my_role_id int;
begin
    select id from sys_role where role_code = input_role_code into my_role_id;
    insert into sys_user_role_relation (user_id, role_id)
    values (input_sid, my_role_id)
    on conflict do nothing;
end;
$$;

create or replace function delete_user_role(input_sid integer, input_role_code character varying) returns void
    language plpgsql
as
$$
declare
    my_role_id int;
begin
    select id from sys_role where role_code = input_role_code into my_role_id;

    delete
    from sys_user_role_relation
    where role_id = my_role_id
      and user_id = input_sid;

end;
$$;

create or replace function get_leaderboard_by_qid_and_num(input_question_id integer, input_num integer)
    returns TABLE
            (
                return_record_sid   character varying,
                return_running_time character varying,
                user_rank           character varying
            )
    language plpgsql
as
$$
begin
    return query
        select cast(record_sid as varchar) as return_record_sid,
               cast(min_time as varchar)   as return_running_time,
               cast(row_number() over (order by min_time) as varchar)
                                           as user_rank
        from (
                 select record_sid, min(running_time) as min_time
                 from record r
                 where record_question_id = input_question_id
                   and running_time > 0
                   and record_status = 1
                 group by record_sid
                 limit input_num) t
        order by user_rank;
end;
$$;

