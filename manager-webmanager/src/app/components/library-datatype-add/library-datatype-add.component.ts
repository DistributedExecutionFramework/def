import {Component, OnInit} from '@angular/core';
import {LibraryService} from '../../services/LibraryService/library.service';
import {ActivatedRoute, Router} from '@angular/router';
import {finalize} from 'rxjs/operators';
import {NavigationElement} from '../../routing/navigation-element';

@Component({
  selector: 'app-library-datatype-add',
  templateUrl: './library-datatype-add.component.html',
  styleUrls: ['./library-datatype-add.component.css']
})
export class LibraryDatatypeAddComponent implements OnInit {

  navigationPath: NavigationElement[] = [];
  activeNavigationElement: NavigationElement;
  name: string;
  schema: string;
  exampleSchema: string;
  submitting = false;
  isHelpCollapsed = true;
  syntaxHighlighting = true;

  constructor(
    private libraryService: LibraryService,
    private router: Router,
    private route: ActivatedRoute
  ) { }

  ngOnInit() {
    // Update navigation path
    this.setNavigationPath();

    this.generateSchema();
    this.generateExampleSchema();
  }

  setNavigationPath(): void {
    this.navigationPath.push(new NavigationElement('Library Overview', '../../', ''));
    this.navigationPath.push(new NavigationElement('Data Types', '../', ''));
    this.activeNavigationElement = new NavigationElement(
      'Add Data Type',
      './',
      ''
    );
  }

  generateSchema(): void {
    this.schema =
      'namespace java at.enfilo.def.datatype\n' +
      'namespace py def_api\n' +
      'typedef string Id\n' +
      '\n' +
      'struct ' + this.name + ' {\n' +
      '  1: optional Id _id = "",\n' +
      '  2: ... ,\n' +
      '}'
    ;
  }

  generateExampleSchema(): void {
    this.exampleSchema =
      'namespace java at.enfilo.def.datatype\n' +
      'namespace py def_api\n' +
      'typedef string Id\n' +
      '\n' +
      'struct DEFString {\n' +
      '  1: optional Id _id = "12345",\n' +
      '  2: string value\n' +
      '}'
    ;
  }

  onSubmit(): void {
    this.submitting = true;
    this.libraryService.createDataType(this.name, this.schema)
      .pipe(finalize(() => {
        this.submitting = false;
        this.router.navigate(['..'], {relativeTo: this.route, queryParams: {dataTypeFilter: this.name}});
      }))
      .subscribe();
  }
}
