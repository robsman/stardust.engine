CREATE OR REPLACE PACKAGE ipp_tools
AS
    TYPE OID_LIST IS TABLE OF NUMBER INDEX BY binary_integer;

    PROCEDURE delete_processes(piOids IN OID_LIST);

    PROCEDURE do_delete_processes;
END ipp_tools;

CREATE OR REPLACE PACKAGE BODY ipp_tools
AS

    /* Must ensure explicitly create the following temp table before the package can be sucessfully compiled:

       CREATE GLOBAL TEMPORARY TABLE ipp_tools$pi_oids_to_delete (oid NUMBER) ON COMMIT DELETE ROWS
    */

    PROCEDURE delete_processes(piOids IN OID_LIST) AS
        piOid NUMBER; -- the current PI OID
        rootPiOid NUMBER; -- the root PI OID for the current PI OID, if existent
        piState NUMBER; -- the state of the current PI OID, if existent
        nDeletedPis NUMBER;
    BEGIN
        -- reset delete set
        DELETE ipp_tools$pi_oids_to_delete;

        nDeletedPis := 0;
        FOR i IN piOids.FIRST .. piOids.LAST
        LOOP
            piOid := piOids(i);

            -- check that provided PI is valid, a root PI and terminated
            BEGIN
                SELECT pi.rootProcessInstance, pi.state
                  INTO rootPiOid, piState
                  FROM process_instance pi
                 WHERE pi.oid = piOid;
            EXCEPTION
            WHEN NO_DATA_FOUND THEN
                RAISE_APPLICATION_ERROR(-20001, 'Process instance ' || piOid || ' does not exist.');
            END;

            IF rootPiOid != piOid THEN
                RAISE_APPLICATION_ERROR(-20001, 'Process instance ' || piOid || ' is no root process instance.');
            ELSIF piState NOT IN (2, 1) THEN
                RAISE_APPLICATION_ERROR(-20001, 'Process instance ' || piOid || ' is not terminated.');
            END IF;

            -- put transitive closure of provided PI into delete set
            INSERT INTO ipp_tools$pi_oids_to_delete (oid) (
                SELECT pi.oid FROM process_instance pi WHERE pi.rootProcessInstance = piOid
            );

            nDeletedPis := nDeletedPis + 1;
        END LOOP;

        DBMS_OUTPUT.PUT_LINE('About to delete ' || nDeletedPis || ' root process instances ...');

        do_delete_processes;

        DBMS_OUTPUT.PUT_LINE('Deleted ' || nDeletedPis || ' root process instances.');
    END delete_processes;

    PROCEDURE do_delete_processes AS
    BEGIN

    -- BEGIN -- generated DELETE scripts
    -- END -- generated DELETE scripts

    END do_delete_processes;

END ipp_tools;