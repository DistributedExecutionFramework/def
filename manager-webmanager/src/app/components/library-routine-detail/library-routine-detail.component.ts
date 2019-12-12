import {Component, OnDestroy, OnInit} from '@angular/core';
import {LibraryService} from '../../services/LibraryService/library.service';
import {ActivatedRoute, Router} from '@angular/router';
import {Language, Routine, RoutineType} from '../../entities/routine';
import {NavigationElement} from '../../routing/navigation-element';
import {DataType} from '../../entities/datatype';

@Component({
  selector: 'app-library-routine-detail',
  templateUrl: './library-routine-detail.component.html',
  styleUrls: ['./library-routine-detail.component.css']
})
export class LibraryRoutineDetailComponent implements OnInit, OnDestroy {
  private readonly JAVA_API: string = '/assets/api/java/client-api-1.4.6-all.jar';
  private readonly PYTHON_API: string = '/assets/api/python/client-routine-api-1.4.6.zip';
  private readonly MATLAB_API: string = '/assets/api/matlab/client-routine-api-1.4.6.zip';

  navigationPath: NavigationElement[] = [];
  activeNavigationElement: NavigationElement;
  rId: string;
  routine: Routine;
  language: Language = Language.JAVA;
  clientExample: string;
  highlightLanguages: string[] = ['java', 'cs', 'python', 'cp', 'c++', 'octave', 'matlab'];
  clientApiUrl: string;
  checkType = RoutineType;

  constructor(
    public libraryService: LibraryService,
    private route: ActivatedRoute,
    private router: Router,
  ) { }

  ngOnInit() {
    this.rId = this.route.snapshot.paramMap.get('rId');

    // Update navigation path
    this.setNavigationPath();

    // Initial fetching of routine
    this.routine = this.libraryService.getSelectedRoutine();
    if (this.routine == null) {
      this.libraryService.getRoutineById(this.rId).subscribe(
        value => {
          this.routine = value;
          this.changeLanguage();
        }
      );
    } else {
      this.changeLanguage();
    }
  }

  ngOnDestroy(): void {
    this.rId = null;
    this.routine = null;
  }


  setNavigationPath(): void {
    this.navigationPath.push(
      new NavigationElement(
        'Library Overview',
        '../../',
        '')
    );
    this.activeNavigationElement = new NavigationElement(
      'Routine Details',
      './',
      this.rId
    );
  }

  getLanguageName(language: Language): string {
    return this.libraryService.getLanguageName(language);
  }

  getRoutineTypeName(routineType: RoutineType): string {
    return this.libraryService.getRoutineTypeName(routineType);
  }

  hasInParameters(): boolean {
    if (!this.routine) {
      return false;
    }
    return (this.routine.type === RoutineType.OBJECTIVE ||
      this.routine.type === RoutineType.MAP ||
      this.routine.type === RoutineType.REDUCE);
  }

  hasOutParameter(): boolean {
    if (!this.routine) {
      return false;
    }
    return (this.routine.type === RoutineType.OBJECTIVE ||
      this.routine.type === RoutineType.MAP ||
      this.routine.type === RoutineType.REDUCE);
  }

  getLinkDataTypeDetails(dataType: DataType): string {
    this.libraryService.selectDataType(dataType);
    return '/manager/library/datatypes/datatype-detail/' + dataType.id;
  }

  getLanguages(): Language[] {
    return this.libraryService.getLanguages();
  }

  showUsage(): boolean {
    return (this.routine && this.routine.type === RoutineType.OBJECTIVE);
  }

  changeLanguage(): void {
    if (this.routine === undefined) {
      return;
    }
    switch (this.language.toString()) {
      case Language.MATLAB.toString():
        this.clientApiUrl = this.MATLAB_API;
        break;
      case Language.JAVA.toString():
        this.clientApiUrl = this.JAVA_API;
        break;
      case Language.PYTHON.toString():
        this.clientApiUrl = this.PYTHON_API;
        break;
      default:
        this.clientApiUrl = '#';
    }
    switch (this.routine.type.toString()) {
      case RoutineType.OBJECTIVE.toString():
        this.updateObjectiveClientExample();
        break;
      case RoutineType.REDUCE.toString():
        this.clientExample = 'TODO';
        break;
      case RoutineType.MAP.toString():
        this.clientExample = 'TODO';
        break;
      case RoutineType.STORE.toString():
        this.clientExample = 'TODO';
        break;
      case RoutineType.CLIENT.toString():
        this.clientExample = 'TODO';
        break;
    }
  }

  private updateObjectiveClientExample(): void {
    switch (this.language.toString()) {
      case Language.JAVA.toString():
        this.highlightLanguages = ['java'];
        this.clientExample =
          'import at.enfilo.def.client.api.DEFClientFactory;\n' +
          'import at.enfilo.def.client.api.IDEFClient;\n' +
          'import at.enfilo.def.client.api.RoutineInstanceBuilder;\n' +
          'import at.enfilo.def.communication.dto.Protocol;\n' +
          'import at.enfilo.def.communication.dto.ServiceEndpointDTO;\n' +
          'import at.enfilo.def.transfer.dto.ExecutionState;\n' +
          'import at.enfilo.def.transfer.dto.SortingCriterion;\n' +
          'import at.enfilo.def.transfer.dto.JobDTO;\n' +
          'import at.enfilo.def.transfer.dto.RoutineInstanceDTO;\n' +
          'import at.enfilo.def.transfer.dto.TaskDTO;\n' +
          '\n' +
          'import java.util.List;\n' +
          'import java.util.concurrent.Future;\n' +
          '\n' +
          'public class Example {\n\n' +
          '\tpublic static void main(String[] args) throws Exception {\n' +
          '\t\t// ...\n' +
          '\t\t// Create client\n' +
          '\t\tServiceEndpointDTO managerEndpoint = new ServiceEndpointDTO("<replace with manager address>", 40002, Protocol.THRIFT_TCP);\n' +
          '\t\tIDEFClient defClient = DEFClientFactory.createClient(managerEndpoint);\n\n' +
          '\t\t// Create program\n' +
          '\t\tFuture<String> fPId = defClient.createProgram("<replace with cluster id>", "<replace with user id>");\n' +
          '\t\tString pId = fPId.get();\n\n' +
          '\t\twhile (/* # jobs */) {\n' +
          '\t\t\t// Create Job.\n' +
          '\t\t\tFuture<String> fJId = defClient.createJob(pId);\n' +
          '\t\t\tString jId = fJId.get();\n' +
          '\t\t\twhile (/* # tasks */) {\n' +
          '\t\t\t\tRoutineInstanceDTO routine = new RoutineInstanceBuilder("' + this.rId + '")\n';
        for (let inParam of this.routine.inParameters) {
          this.clientExample += '\t\t\t\t\t\t.addParameter("' + inParam.name + '", new '+ inParam.dataType.name +'(/* value */))\n';
        }
        this.clientExample += '\t\t\t\t\t\t.build();\n' +
          '\t\t\t\tdefClient.createTask(pId, jId, routine);\n' +
          '\t\t\t}\n\n' +
          '\t\t\tdefClient.markJobAsComplete(pId, jId);\n' +
          '\t\t\tJobDTO job = defClient.waitForJob(pId, jId); // Blocking call: waits if job reach the state "SUCCESS" or "FAILED".\n' +
          '\t\t\tif (job.getState() == ExecutionState.SUCCESS) {\n' +
          '\t\t\t\t// Fetch all tasks and results.\n' +
          '\t\t\t\tList<String> tIds = defClient.getAllTasksWithState(pId, jId, ExecutionState.SUCCESS, SortingCriterion.NO_SORTING).get();\n' +
          '\t\t\t\tfor (String tId : tIds) {\n' +
          '\t\t\t\t\tTaskDTO task = defClient.getTask(pId, jId, tId).get();\n' +
          '\t\t\t\t\t' + this.routine.outParameter.dataType.name +' result = defClient.extractOutParameter(task, ' + this.routine.outParameter.dataType.name + '.class);\n' +
          '\t\t\t\t\t// Process task result.\n' +
          '\t\t\t\t}\n' +
          '\t\t\t}\n' +
          '\t\t\tdefClient.deleteJob(pId, jId); // Optional\n' +
          '\t\t}\n\n' +
          '\t\tdefClient.markProgramAsFinished(pId);\n' +
          '\t\tdefClient.deleteProgram(pId); // Optional: Delete all resources\n' +
          '\t}\n}\n';
        break;
      case Language.MATLAB.toString():
        this.highlightLanguages = ['matlab'];
        this.clientExample =
          'clear java;\n' +
          'addpath(\'client\');\n' +
          'addpath(\'datatypes\');\n\n' +
          '% ...\n\n' +
          '% Create DEF client instance\n' +
          'client = DEFClient(\'<replace with manager address>\', 40002, \'THRIFT_TCP\');\n' +
          '\n' +
          '% Create a new Program\n' +
          'future_pId = createProgram(client, \'<replace with cluster id>\', \'<replace with user id>\');\n' +
          'pId = get(future_pId);\n' +
          '\n' +
          'while true % Job loop\n' +
          '\t% Create a new Job\n' +
          '\tfuture_jId = createJob(client, pId);\n' +
          '\tjId = get(future_jId);\n' +
          '\n' +
          '\twhile true % Task loop\n' +
          '\t\t% Create routine instance and task\n' +
          '\t\troutineInstance = RoutineInstanceBuilder(\'' + this.routine.id + '\');\n';
        for (const inParam of this.routine.inParameters) {
          this.clientExample += '\t\taddParameter(routineInstance, \'' + inParam.name + '\', ' + inParam.dataType.name + '(someValue));\n';
        }
        this.clientExample +=
          '\t\tcreateTask(client, pId, jId, routineInstance);\n' +
          '\tend\n' +
          '\n' +
          '\t% Mark job as complete\n' +
          '\tmarkJobAsComplete(client, pId, jId);\n' +
          '\n' +
          '\t% ----------------------------------\n' +
          '\n' +
          '\t% Wait for Job\n' +
          '\tjob = waitForJob(client, pId, jId);\n' +
          '\tif strcmp(job.State, \'SUCCESS\')\n' +
          '\t\t% Fetch all tasks and results\n' +
          '\t\tfuture_tIds = getAllTasksWithState(client, pId, jId, \'SUCCESS\', \'NO_SORTING\');\n' +
          '\t\ttIds = get(future_tIds);\n' +
          '\t\tfor tId = tIds.Items\n' +
          '\t\t\tfuture_task = getTask(client, pId, jId, tId);\n' +
          '\t\t\ttask = get(future_task);\n' +
          '\t\t\tresult = extractOutParameter(client, task, \'' + this.routine.outParameter.dataType.name + '\');\n' +
          '\t\t\t% Process task result.\n' +
          '\t\tend\n' +
          '\tend\n' +
          '\tdeleteJob(client, pId, jId); % Optional\n' +
          'end\n' +
          'markProgramAsFinished(client, pId);\n' +
          'deleteProgram(client, pId); % Optional: Delete all resources\n';
        break;
      case Language.PYTHON.toString():
        this.highlightLanguages = ['python'];
        this.clientExample =
          'import asyncio\n' +
          'from def_api.client import DEFClient\n' +
          'from def_api.client_helper import RoutineInstanceBuilder, extract_result\n' +
          'from def_api.thrift.communication.ttypes import Protocol\n' +
          'from def_api.thrift.transfer.ttypes import *\n' +
          'from def_api.ttypes import *\n\n' +
          '# ...\n\n' +
          '# Setup asyncio for future support.\n' +
          'loop = asyncio.get_event_loop()\n' +
          '# create client\n' +
          'client = DEFClient(host=\'<replace with manager address>\', port=40002, protocol=Protocol.THRIFT_TCP)\n' +
          '\n' +
          '# Create program\n' +
          'future_p_id = client.create_program(\'<replace with clusterId>\', \'<replace with userId>\')\n' +
          'loop.run_until_complete(future_p_id)\n' +
          'p_id = future_p_id.result()\n' +
          '\n' +
          'while True: # TODO: Job loop\n' +
          '\t# Create job\n' +
          '\tfuture_j_id = client.create_job(p_id)\n' +
          '\tloop.run_until_complete(future_j_id)\n' +
          '\tj_id = future_j_id.result()\n' +
          '\n' +
          '\twhile True: # TODO: Task loop\n' +
          '\t\t# Prepare routine instance and create a task\n' +
          '\t\tbuilder = RoutineInstanceBuilder(\'' + this.routine.id + '\')\n';
        for (let inParam of this.routine.inParameters) {
          this.clientExample += '\t\tbuilder.add_parameter(\'' + inParam.name + '\', '+ inParam.dataType.name +'()) # TODO: Attach values to data type\n';
        }
        this.clientExample +=
          '\t\tclient.create_task(p_id, j_id, builder.get_routine_instance())\n' +
          '\n' +
          '\tclient.mark_job_as_complete(p_id, j_id)\n' +
          '\tstate = client.wait_for_job_finished(p_id, j_id) # Blocking call which waits to job reach the state SUCCESS or FAILED.\n\n' +
          '\tif state == ExecutionState.SUCCESS:\n' +
          '\t\t# Fetch all tasks and task results.\n' +
          '\t\tfuture_t_ids = client.get_all_tasks_with_state(p_id, j_id, ExecutionState.SUCCESS)\n' +
          '\t\tloop.run_until_complete(future_t_ids)\n' +
          '\t\tt_ids = future_t_ids.result()\n' +
          '\t\tfor t_id in t_ids:\n' +
          '\t\t\tfuture_t_info = client.get_task(p_id, j_id, t_id)\n' +
          '\t\t\tloop.run_until_complete(future_t_info)\n' +
          '\t\t\ttask = future_t_info.result()\n' +
          '\t\t\ttask_result = extract_result(task, ' + this.routine.outParameter.dataType.name + '())\n' +
          '\t\t\t# TODO: Process task result\n\n' +
          '\tclient.delete_job(p_id, j_id) # Optional\n' +
          'client.mark_program_as_finished(p_id)\n' +
          'client.delete_program(p_id) # Optional\n';
        break;
      default:
        this.clientExample = '\n\n---- NOT SUPPORTED ----\n\n';
        break;
    }
  }

  downloadAPI(): void {
    switch (this.language.toString()) {
      case Language.JAVA.toString():
        this.router.navigate([this.JAVA_API]);
        break;
      case Language.PYTHON.toString():
        this.router.navigate([this.PYTHON_API]);
        break;
      case Language.MATLAB.toString():
        this.router.navigate([this.MATLAB_API]);
        break;
      default:
        break;
    }
  }

  generateAndDownloadDataTypes(): void {
    const dIds: string[] = [];
    if (this.routine.outParameter) {
      dIds.push(this.routine.outParameter.dataType.id);
    }
    if (this.routine.inParameters) {
      for (const inParam of this.routine.inParameters) {
        dIds.push(inParam.dataType.id);
      }
    }
    this.libraryService.generateAndDownloadDataTypes(this.getLanguageName(this.language), dIds);
  }

  actionToDisplay(): boolean {
    if (this.routine.type !== RoutineType.STORE) {
      return false;
    }
    return true;
  }

  removeRoutine(): void {
    if (this.routine.type === RoutineType.OBJECTIVE) {
      this.libraryService.removeRoutine(this.routine.id)
        .subscribe(
          () => this.router.navigate(['../../'], {relativeTo: this.route, queryParams: {routineFilter: ''}})
        );
    }
  }
}
