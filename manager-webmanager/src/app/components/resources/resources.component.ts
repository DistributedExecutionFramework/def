import { Component, OnInit } from '@angular/core';
import {NavigationElement} from "../../routing/navigation-element";

@Component({
  selector: 'app-resources',
  templateUrl: './resources.component.html',
  styleUrls: ['./resources.component.css']
})
export class ResourcesComponent implements OnInit {

  activeNavigationElement: NavigationElement;

  constructor() { }

  ngOnInit() {
    this.setNavigationPath();
  }

  private setNavigationPath() {
    this.activeNavigationElement = new NavigationElement(
      'Resources',
      './',
      ''
    );
  }

}
