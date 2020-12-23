
--  6 modify assignment
select add_url('/user/queryAssigmentList', 13);
select add_url('/user/selectAssignmentById', 13);
select add_url('/user/queryQuestionsByAssignmentID', 13);
select add_url('/admin/addAssignment', 6);
select add_url('/admin/updateAssignment', 6);
select add_url('/admin/deleteAssignment', 6);


select add_url('/user/selectQuestionsById', 13);
select add_url('/user/selectQuestionsByAssignment', 13);

-- 9 is modify question
select add_url('/admin/queryQuestionList', 9);
select add_url('/admin/addQuestion', 9);
select add_url('/admin/addQuestionTrigger', 9);
select add_url('/admin/updateQuestion', 9);
select add_url('/admin/updateQuestionTrigger', 9);
select add_url('/admin/deleteQuestion', 9);


-- 14 modify user
select add_url('/admin/queryUserList', 14);
select add_url('/admin/findUserBySid', 14);


-- user level operation
select add_url('/user/resetPwd', 13);
select add_url('/user/sendVerifyCode', 13);
select add_url('/user/loginCountToday', 13);
select add_url('/user/getRecordCountForAWeek', 13);
select add_url('/user/getLeaderBoardByQid', 13);



-- 15 modify files
select add_url('/admin/files/findByUserId', 15);
select add_url('/admin/files/deleteFile', 15);
select add_url('/admin/files/download', 15);
select add_url( '/admin/files/upload', 15);
select add_url('/admin/files/showAllFiles', 15);
select add_url('/admin/files/uploadToRemoteDatabase', 15);
select add_url('/admin/files/uploadToRemote', 15);
select add_url('/admin/initDatabaseDocker', 15);

select add_url('/admin/deleteDatabaseById', 15);
select add_url('/admin/copyToRemote', 15);
select add_url('/admin/queryDatabaseList', 15);

-- 16 check_similarity
-- '/admin/checkSimilarityByQid'


insert into sys_role_permission_relation(role_id, permission_id)
values (1, 15);

insert into sys_role_permission_relation(role_id, permission_id)
values (2, 15);