import {Component, OnInit} from '@angular/core';
import {LibraryService} from '../../services/LibraryService/library.service';
import {ActivatedRoute, Router} from '@angular/router';
import {finalize} from 'rxjs/operators';
import {NavigationElement} from '../../routing/navigation-element';

@Component({
  selector: 'app-library-feature-add',
  templateUrl: './library-feature-add.component.html',
  styleUrls: ['./library-feature-add.component.css']
})
export class LibraryFeatureAddComponent implements OnInit {

  navigationPath: NavigationElement[] = [];
  activeNavigationElement: NavigationElement;
  id: string;
  name: string;
  version: string;
  group: string;
  submitting = false;

  constructor(
    private libraryService: LibraryService,
    private router: Router,
    private route: ActivatedRoute
  ) { }

  ngOnInit() {
    // Update navigation path
    this.setNavigationPath();
  }

  setNavigationPath(): void {
    this.navigationPath.push(new NavigationElement('Library Overview', '../../', ''));
    this.navigationPath.push(new NavigationElement('Features', '../', ''));
    this.activeNavigationElement = new NavigationElement(
      'Add Feature',
      './',
      ''
    );
  }

  onSubmit(): void {
    this.submitting = true;
    this.libraryService.createFeature(this.name, this.version, this.group)
      .pipe(finalize(() => {
        this.submitting = false;
        this.router.navigate(['../'], {relativeTo: this.route, queryParams: {featureFilter: this.id}});
      }))
      .subscribe(id => this.id = id);
  }
}
