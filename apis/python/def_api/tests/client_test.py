import uuid
from unittest import TestCase
from unittest import mock

from def_api.client import *
from def_api.thrift.transfer.ttypes import *
from def_api.ttypes import DEFDouble


class DEFClientTest(TestCase):

    def setUp(self):
        self.loop = asyncio.get_event_loop()
        self.request_client = mock.MagicMock()
        self.ticket_client = mock.MagicMock()
        self.response_client = mock.MagicMock()
        self.client = DEFClient(request_client=self.request_client, response_client=self.response_client, ticket_client=self.ticket_client)

    def test_get_all_programs(self):
        ticket_id = str(uuid.uuid4())
        user_id = str(uuid.uuid4())
        p_ids = [str(uuid.uuid4()), str(uuid.uuid4())]
        # Mocking methods
        self.request_client.getAllPrograms.return_value = ticket_id
        self.ticket_client.getTicketStatus.return_value = TicketStatusDTO.DONE
        self.response_client.getAllPrograms.return_value = p_ids
        # Get all programs
        future = self.client.get_all_programs(user_id)
        self.loop.run_until_complete(future)
        self.assertEqual(p_ids, future.result())
        # Mock assertions
        self.request_client.getAllPrograms.assert_called_with(user_id)
        self.ticket_client.getTicketStatus.assert_called_with(ticket_id)
        self.response_client.getAllPrograms.assert_called_with(ticket_id)

    def test_create_program(self):
        ticket_id = str(uuid.uuid4())
        cluster_id = str(uuid.uuid4())
        user_id = str(uuid.uuid4())
        p_id = str(uuid.uuid4())
        # Mocking methods
        self.request_client.createProgram.return_value = ticket_id
        self.ticket_client.getTicketStatus.return_value = TicketStatusDTO.DONE
        self.response_client.createProgram.return_value = p_id
        # Create program
        future = self.client.create_program(cluster_id, user_id)
        self.loop.run_until_complete(future)
        self.assertEqual(p_id, future.result())
        # Mock assertions
        self.request_client.createProgram.assert_called_with(cluster_id, user_id)
        self.ticket_client.getTicketStatus.assert_called_with(ticket_id)
        self.response_client.createProgram.assert_called_with(ticket_id)

    def test_get_program(self):
        ticket_id = str(uuid.uuid4())
        p_id = str(uuid.uuid4())
        p = ProgramDTO()
        p.id = p_id
        # Mocking methods
        self.request_client.getProgram.return_value = ticket_id
        self.ticket_client.getTicketStatus.return_value = TicketStatusDTO.DONE
        self.response_client.getProgram.return_value = p
        # Get program
        future = self.client.get_program(p_id)
        self.loop.run_until_complete(future)
        self.assertEqual(p, future.result())
        # Mock assertions
        self.request_client.getProgram.assert_called_with(p_id)
        self.ticket_client.getTicketStatus.assert_called_with(ticket_id)
        self.response_client.getProgram.assert_called_with(ticket_id)

    def test_delete_program(self):
        ticket_id = str(uuid.uuid4())
        p_id = str(uuid.uuid4())
        # Mocking methods
        self.request_client.deleteProgram.return_value = ticket_id
        self.ticket_client.getTicketStatus.return_value = TicketStatusDTO.DONE
        # Delete program
        future = self.client.delete_program(p_id)
        self.loop.run_until_complete(future)
        self.assertEqual(TicketStatusDTO.DONE, future.result())
        # Mock assertions
        self.request_client.deleteProgram.assert_called_with(p_id)
        self.ticket_client.getTicketStatus.assert_called_with(ticket_id)

    def test_mark_program_as_finished(self):
        ticket_id = str(uuid.uuid4())
        p_id = str(uuid.uuid4())
        # Mocking methods
        self.request_client.markProgramAsFinished.return_value = ticket_id
        self.ticket_client.getTicketStatus.return_value = TicketStatusDTO.DONE
        # Mark program
        future = self.client.mark_program_as_finished(p_id)
        self.loop.run_until_complete(future)
        self.assertEqual(TicketStatusDTO.DONE, future.result())
        # Mock assertions
        self.request_client.markProgramAsFinished.assert_called_with(p_id)
        self.ticket_client.getTicketStatus.assert_called_with(ticket_id)

    def test_get_all_jobs(self):
        ticket_id = str(uuid.uuid4())
        p_id = str(uuid.uuid4())
        j_ids = [str(uuid.uuid4()), str(uuid.uuid4())]
        # Mocking methods
        self.request_client.getAllJobs.return_value = ticket_id
        self.ticket_client.getTicketStatus.return_value = TicketStatusDTO.DONE
        self.response_client.getAllJobs.return_value = j_ids
        # Get all jobs
        future = self.client.get_all_jobs(p_id)
        self.loop.run_until_complete(future)
        self.assertEqual(j_ids, future.result())
        # Mock assertions
        self.request_client.getAllJobs.assert_called_with(p_id)
        self.ticket_client.getTicketStatus.assert_called_with(ticket_id)
        self.response_client.getAllJobs.assert_called_with(ticket_id)

    def test_create_job(self):
        ticket_id = str(uuid.uuid4())
        p_id = str(uuid.uuid4())
        j_id = str(uuid.uuid4())
        # Mocking methods
        self.request_client.createJob.return_value = ticket_id
        self.ticket_client.getTicketStatus.return_value = TicketStatusDTO.DONE
        self.response_client.createJob.return_value = j_id
        # Create jobs
        future = self.client.create_job(p_id)
        self.loop.run_until_complete(future)
        self.assertEqual(j_id, future.result())
        # Mock assertions
        self.request_client.createJob.assert_called_with(p_id)
        self.ticket_client.getTicketStatus.assert_called_with(ticket_id)
        self.response_client.createJob.assert_called_with(ticket_id)

    def test_get_job(self):
        ticket_id = str(uuid.uuid4())
        p_id = str(uuid.uuid4())
        j_id = str(uuid.uuid4())
        j = JobDTO()
        j.id = j_id
        # Mocking methods
        self.request_client.createJob.return_value = ticket_id
        self.ticket_client.getTicketStatus.return_value = TicketStatusDTO.DONE
        self.response_client.createJob.return_value = j_id
        # Create jobs
        future = self.client.create_job(p_id)
        self.loop.run_until_complete(future)
        self.assertEqual(j_id, future.result())
        # Mock assertions
        self.request_client.createJob.assert_called_with(p_id)
        self.ticket_client.getTicketStatus.assert_called_with(ticket_id)
        self.response_client.createJob.assert_called_with(ticket_id)

    def test_delete_job(self):
        ticket_id = str(uuid.uuid4())
        p_id = str(uuid.uuid4())
        j_id = str(uuid.uuid4())
        # Mocking methods
        self.request_client.deleteJob.return_value = ticket_id
        self.ticket_client.getTicketStatus.return_value = TicketStatusDTO.DONE
        # Delete job
        future = self.client.delete_job(p_id, j_id)
        self.loop.run_until_complete(future)
        self.assertEqual(TicketStatusDTO.DONE, future.result())
        # Mock assertions
        self.request_client.deleteJob.assert_called_with(p_id, j_id)
        self.ticket_client.getTicketStatus.assert_called_with(ticket_id)

    def test_get_attached_map_routine(self):
        ticket_id = str(uuid.uuid4())
        p_id = str(uuid.uuid4())
        j_id = str(uuid.uuid4())
        routine_id = str(uuid.uuid4())
        # Mocking methods
        self.request_client.getAttachedMapRoutine.return_value = ticket_id
        self.ticket_client.getTicketStatus.return_value = TicketStatusDTO.DONE
        self.response_client.getAttachedMapRoutine.return_value = routine_id
        # Get attached map routine from jobs
        future = self.client.get_attached_map_routine(p_id, j_id)
        self.loop.run_until_complete(future)
        self.assertEqual(routine_id, future.result())
        # Mock assertions
        self.request_client.getAttachedMapRoutine.assert_called_with(p_id, j_id)
        self.ticket_client.getTicketStatus.assert_called_with(ticket_id)
        self.response_client.getAttachedMapRoutine.assert_called_with(ticket_id)

    def test_attach_map_routine(self):
        ticket_id = str(uuid.uuid4())
        p_id = str(uuid.uuid4())
        j_id = str(uuid.uuid4())
        routine_id = str(uuid.uuid4())
        # Mocking methods
        self.request_client.attachMapRoutine.return_value = ticket_id
        self.ticket_client.getTicketStatus.return_value = TicketStatusDTO.DONE
        # Attach map routine to job
        future = self.client.attach_map_routine(p_id, j_id, routine_id)
        self.loop.run_until_complete(future)
        self.assertEqual(TicketStatusDTO.DONE, future.result())
        # Mock assertions
        self.request_client.attachMapRoutine.assert_called_with(p_id, j_id, routine_id)
        self.ticket_client.getTicketStatus.assert_called_with(ticket_id)

    def test_get_attached_reduce_routine(self):
        ticket_id = str(uuid.uuid4())
        p_id = str(uuid.uuid4())
        j_id = str(uuid.uuid4())
        routine_id = str(uuid.uuid4())
        # Mocking methods
        self.request_client.getAttachedReduceRoutine.return_value = ticket_id
        self.ticket_client.getTicketStatus.return_value = TicketStatusDTO.DONE
        self.response_client.getAttachedReduceRoutine.return_value = routine_id
        # Get attached reduce routine from job
        future = self.client.get_attached_reduce_routine(p_id, j_id)
        self.loop.run_until_complete(future)
        self.assertEqual(routine_id, future.result())
        # Mock assertions
        self.request_client.getAttachedReduceRoutine.assert_called_with(p_id, j_id)
        self.ticket_client.getTicketStatus.assert_called_with(ticket_id)
        self.response_client.getAttachedReduceRoutine.assert_called_with(ticket_id)

    def test_attach_map_routine(self):
        ticket_id = str(uuid.uuid4())
        p_id = str(uuid.uuid4())
        j_id = str(uuid.uuid4())
        routine_id = str(uuid.uuid4())
        # Mocking methods
        self.request_client.attachReduceRoutine.return_value = ticket_id
        self.ticket_client.getTicketStatus.return_value = TicketStatusDTO.DONE
        # Attach reduce routine to job
        future = self.client.attach_reduce_routine(p_id, j_id, routine_id)
        self.loop.run_until_complete(future)
        self.assertEqual(TicketStatusDTO.DONE, future.result())
        # Mock assertions
        self.request_client.attachReduceRoutine.assert_called_with(p_id, j_id, routine_id)
        self.ticket_client.getTicketStatus.assert_called_with(ticket_id)

    def test_mark_job_as_complete(self):
        ticket_id = str(uuid.uuid4())
        p_id = str(uuid.uuid4())
        j_id = str(uuid.uuid4())
        # Mocking methods
        self.request_client.markJobAsComplete.return_value = ticket_id
        self.ticket_client.getTicketStatus.return_value = TicketStatusDTO.DONE
        # Mark job as complete
        future = self.client.mark_job_as_complete(p_id, j_id)
        self.loop.run_until_complete(future)
        self.assertEqual(TicketStatusDTO.DONE, future.result())
        # Mock assertions
        self.request_client.markJobAsComplete.assert_called_with(p_id, j_id)
        self.ticket_client.getTicketStatus.assert_called_with(ticket_id)

    def test_abort_job(self):
        ticket_id = str(uuid.uuid4())
        p_id = str(uuid.uuid4())
        j_id = str(uuid.uuid4())
        # Mocking methods
        self.request_client.abortJob.return_value = ticket_id
        self.ticket_client.getTicketStatus.return_value = TicketStatusDTO.DONE
        # Abort job
        future = self.client.abort_job(p_id, j_id)
        self.loop.run_until_complete(future)
        self.assertEqual(TicketStatusDTO.DONE, future.result())
        # Mock assertions
        self.request_client.abortJob.assert_called_with(p_id, j_id)
        self.ticket_client.getTicketStatus.assert_called_with(ticket_id)

    def test_get_all_tasks(self):
        ticket_id = str(uuid.uuid4())
        p_id = str(uuid.uuid4())
        j_id = str(uuid.uuid4())
        t_ids = [str(uuid.uuid4()), str(uuid.uuid4())]
        # Mocking methods
        self.request_client.getAllTasks.return_value = ticket_id
        self.ticket_client.getTicketStatus.return_value = TicketStatusDTO.DONE
        self.response_client.getAllTasks.return_value = t_ids
        # Get all tasks
        future = self.client.get_all_tasks(p_id, j_id, SortingCriterion.NO_SORTING)
        self.loop.run_until_complete(future)
        self.assertEqual(t_ids, future.result())
        # Mock assertions
        self.request_client.getAllTasks.assert_called_with(p_id, j_id, SortingCriterion.NO_SORTING)
        self.ticket_client.getTicketStatus.assert_called_with(ticket_id)
        self.response_client.getAllTasks.assert_called_with(ticket_id)

    def test_create_task(self):
        ticket_id = str(uuid.uuid4())
        p_id = str(uuid.uuid4())
        j_id = str(uuid.uuid4())
        t_id = str(uuid.uuid4())
        routine_instance = RoutineInstanceDTO()
        # Mocking methods
        self.request_client.createTask.return_value = ticket_id
        self.ticket_client.getTicketStatus.return_value = TicketStatusDTO.DONE
        self.response_client.createTask.return_value = t_id
        # Create Task
        future = self.client.create_task(p_id, j_id, routine_instance)
        self.loop.run_until_complete(future)
        self.assertEqual(t_id, future.result())
        # Mock assertions
        self.request_client.createTask.assert_called_with(p_id, j_id, routine_instance)
        self.ticket_client.getTicketStatus.assert_called_with(ticket_id)
        self.response_client.createTask.assert_called_with(ticket_id)

    def test_get_task(self):
        ticket_id = str(uuid.uuid4())
        p_id = str(uuid.uuid4())
        j_id = str(uuid.uuid4())
        t_id = str(uuid.uuid4())
        t = TaskDTO()
        t.id = t_id;
        # Mocking methods
        self.request_client.getTask.return_value = ticket_id
        self.ticket_client.getTicketStatus.return_value = TicketStatusDTO.DONE
        self.response_client.getTask.return_value = t
        # Create Task
        future = self.client.get_task(p_id, j_id, t_id)
        self.loop.run_until_complete(future)
        self.assertEqual(t, future.result())
        # Mock assertions
        self.request_client.getTask.assert_called_with(p_id, j_id, t_id)
        self.ticket_client.getTicketStatus.assert_called_with(ticket_id)
        self.response_client.getTask.assert_called_with(ticket_id)

    def test_get_all_shared_resources(self):
        ticket_id = str(uuid.uuid4())
        p_id = str(uuid.uuid4())
        resource_ids = [str(uuid.uuid4()), str(uuid.uuid4())]
        # Mocking methods
        self.request_client.getAllSharedResources.return_value = ticket_id
        self.ticket_client.getTicketStatus.return_value = TicketStatusDTO.DONE
        self.response_client.getAllSharedResources.return_value = resource_ids
        # Get resource ids
        future = self.client.get_all_shared_resources(p_id)
        self.loop.run_until_complete(future)
        self.assertEqual(resource_ids, future.result())
        # Mock assertions
        self.request_client.getAllSharedResources.assert_called_with(p_id)
        self.ticket_client.getTicketStatus.assert_called_with(ticket_id)
        self.response_client.getAllSharedResources.assert_called_with(ticket_id)

    def test_create_shared_resource(self):
        ticket_id = str(uuid.uuid4())
        p_id = str(uuid.uuid4())
        resource_id = str(uuid.uuid4())
        value = DEFDouble(value=123.456)
        # Mocking methods
        self.request_client.createSharedResource.return_value = ticket_id
        self.ticket_client.getTicketStatus.return_value = TicketStatusDTO.DONE
        self.response_client.createSharedResource.return_value = resource_id
        # Create shared resource
        future = self.client.create_shared_resource(p_id, value)
        self.loop.run_until_complete(future)
        self.assertEqual(resource_id, future.result())
        # Mock assertions
        self.request_client.createSharedResource.assert_called_with(p_id, value._id, TSerialization.serialize(value))
        self.ticket_client.getTicketStatus.assert_called_with(ticket_id)
        self.response_client.createSharedResource.assert_called_with(ticket_id)

    def test_get_shared_resource(self):
        ticket_id = str(uuid.uuid4())
        p_id = str(uuid.uuid4())
        resource_id = str(uuid.uuid4())
        resource = ResourceDTO()
        resource.id = resource_id
        # Mocking methods
        self.request_client.getSharedResource.return_value = ticket_id
        self.ticket_client.getTicketStatus.return_value = TicketStatusDTO.DONE
        self.response_client.getSharedResource.return_value = resource
        # Get shared resource
        future = self.client.get_shared_resource(p_id, resource_id)
        self.loop.run_until_complete(future)
        self.assertEqual(resource, future.result())
        # Mock assertions
        self.request_client.getSharedResource.assert_called_with(p_id, resource_id)
        self.ticket_client.getTicketStatus.assert_called_with(ticket_id)
        self.response_client.getSharedResource.assert_called_with(ticket_id)

    def test_delete_shared_resource(self):
        ticket_id = str(uuid.uuid4())
        p_id = str(uuid.uuid4())
        resource_id = str(uuid.uuid4())
        # Mocking methods
        self.request_client.deleteSharedResource.return_value = ticket_id
        self.ticket_client.getTicketStatus.return_value = TicketStatusDTO.DONE
        # Delete shared resource
        future = self.client.delete_shared_resource(p_id, resource_id)
        self.loop.run_until_complete(future)
        self.assertEqual(TicketStatusDTO.DONE, future.result())
        # Mock assertions
        self.request_client.deleteSharedResource.assert_called_with(p_id, resource_id)
        self.ticket_client.getTicketStatus.assert_called_with(ticket_id)

    def test_failed_request(self):
        ticket_id = str(uuid.uuid4())
        p_id = str(uuid.uuid4())
        resource_id = str(uuid.uuid4())
        # Mocking methods
        self.request_client.deleteSharedResource.return_value = ticket_id
        self.ticket_client.getTicketStatus.return_value = TicketStatusDTO.FAILED
        # Delete shared resource
        future = self.client.delete_shared_resource(p_id, resource_id)
        self.loop.run_until_complete(future)
        self.assertEqual(TicketStatusDTO.FAILED, future.result())

    def test_canceled_request(self):
        ticket_id = str(uuid.uuid4())
        p_id = str(uuid.uuid4())
        resource_id = str(uuid.uuid4())
        # Mocking methods
        self.request_client.deleteSharedResource.return_value = ticket_id
        self.ticket_client.getTicketStatus.return_value = TicketStatusDTO.CANCELED
        # Delete shared resource
        future = self.client.delete_shared_resource(p_id, resource_id)
        with self.assertRaises(Exception) as context:
            self.loop.run_until_complete(future)
        self.assertIsNotNone(context)


if __name__ == '__main__':
    unittest.main()
