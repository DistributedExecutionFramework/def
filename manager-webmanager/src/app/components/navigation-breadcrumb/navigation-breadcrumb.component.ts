import {Component, Input, OnInit} from '@angular/core';
import { NavigationElement } from "../../routing/navigation-element";

@Component({
  selector: 'app-navigation-breadcrumb',
  templateUrl: './navigation-breadcrumb.component.html',
  styleUrls: ['./navigation-breadcrumb.component.css']
})
export class NavigationBreadcrumbComponent implements OnInit {

  @Input() navigationPath: NavigationElement[];
  @Input() activeNavigationElement: NavigationElement;

  constructor() { }

  ngOnInit() {
  }

}
