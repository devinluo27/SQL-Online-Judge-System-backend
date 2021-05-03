

select tt.record_sid, u.user_name, tt.finished from
    (select record_sid,  string_agg(question_index, ',')  finished from(
        select distinct r.record_sid, q.question_of_assignment, cast(q.question_index as varchar) question_index,
                 r.record_question_id
        from record r
        join question q on q.question_id = r.record_question_id
        where record_status = 1 and q.question_of_assignment = 106
            order by r.record_sid
    )
        t group by record_sid ) tt join user_db u on u.sid = tt.record_sid;


select distinct u.user_name, r.record_sid, q.question_of_assignment, q.question_index, r.record_question_id
from record r
         join user_db u on u.sid = record_sid
         join question q on q.question_id = r.record_question_id
where record_status = 1 and q.question_of_assignment = 106
order by r.record_sid, question_index;



select '2021SP A2' as  assignment,  u.lab_num,
       u.sid as sid, u.user_name as name,
       tt.correct, coalesce(tt.total_count, 0), coalesce(tt.total_score, 0) from
(
select record_sid,  string_agg(question_index, ',') as correct , count(*) total_count, sum(t.question_score) total_score
from(
    select distinct r.record_sid, q.question_of_assignment, cast(q.question_index as varchar) question_index,
                       r.record_question_id, q.question_score
       from record r
                join question q on q.question_id = r.record_question_id
       where record_status = 1 and q.question_of_assignment = 107
       order by r.record_sid
   )
t group by record_sid)
tt right outer join user_db u on u.sid = tt.record_sid
where u.lab_num in (1, 2, 3, 4, 5)
order by u.lab_num, sid;





select '2021SP A1' as  assignment,  u.lab_num,
       u.sid as sid, u.user_name as name,
       tt.correct, coalesce(tt.total_count, 0), coalesce(tt.total_score, 0) from
    (
        select record_sid,  string_agg(question_index, ',') correct , count(*) total_count, sum(t.question_score) total_score
        from(
                select distinct r.record_sid, q.question_of_assignment, cast(q.question_index as varchar) question_index,
                                r.record_question_id, q.question_score
                from record r
                         join question q on q.question_id = r.record_question_id
                where record_status = 1 and q.question_of_assignment = 106
                order by r.record_sid
            )
                t group by record_sid)
        tt right outer join user_db u on u.sid = tt.record_sid
where u.lab_num in (1, 2, 3, 4, 5)
order by u.lab_num, sid;



select '2021SP A3' as  assignment,  u.lab_num,
       u.sid as sid, u.user_name as name,
       tt.correct, coalesce(tt.total_count, 0) as correct_count, coalesce(tt.total_score, 0) as total_score from
    (
        select record_sid,  string_agg(question_index, ',') correct , count(*) total_count, sum(t.question_score) total_score
        from(
                select distinct r.record_sid, q.question_of_assignment, cast(q.question_index as varchar) question_index,
                                r.record_question_id, q.question_score
                from record r
                         join question q on q.question_id = r.record_question_id
                where record_status = 1 and q.question_of_assignment = 108
                order by r.record_sid
            )
                t group by record_sid)
        tt right outer join user_db u on u.sid = tt.record_sid
where u.lab_num in (1, 2, 3, 4, 5)
order by u.lab_num, sid;







select *
from record
where record_sid = 11811013
and record_status = 1 and record_question_id in (
    select question_id from question where question_of_assignment = 107
);



select * from user_db u  where u.lab_num in (1, 2, 3, 4, 5);
