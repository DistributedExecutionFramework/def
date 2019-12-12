import { Component, OnInit } from '@angular/core';
import {DataType} from '../../entities/datatype';
import {LibraryService} from '../../services/LibraryService/library.service';
import {ActivatedRoute, Router} from '@angular/router';
import {NavigationElement} from '../../routing/navigation-element';

@Component({
  selector: 'app-library-datatype-detail',
  templateUrl: './library-datatype-detail.component.html',
  styleUrls: ['./library-datatype-detail.component.css']
})
export class LibraryDatatypeDetailComponent implements OnInit {

  private dId: string;

  navigationPath: NavigationElement[] = [];
  activeNavigationElement: NavigationElement;
  dataType: DataType;
  language = 'Java';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private libraryService: LibraryService
  ) { }

  ngOnInit() {
    this.dId = this.route.snapshot.paramMap.get('dId');

    // Update navigation path
    this.setNavigationPath();

    // Initial fetching of dataType
    this.dataType = this.libraryService.getSelectedDataType();
    if (this.dataType == null || this.dataType.id != this.dId) {
      this.libraryService.getDataTypeById(this.dId).subscribe(
        value => this.dataType = value
      );
    }
  }

  setNavigationPath(): void {
    this.navigationPath.push(
      new NavigationElement(
        'Library Overview',
        '../../',
        '')
    );
    this.activeNavigationElement = new NavigationElement(
      'Datatype Details',
      './',
      this.dId
    );
  }

  generateAndDownloadDataTypes(): void {
    this.libraryService.generateAndDownloadDataTypes(this.language, [this.dataType.id]);
  }

}
