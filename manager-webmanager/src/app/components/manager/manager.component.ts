import { Component, OnInit } from '@angular/core';
import {MenuSelection} from "../../enums/menu-selection";
import {Location} from "@angular/common";
import {NavigationStart, Router} from "@angular/router";
import {LoginService} from "../../services/LoginService/login.service";

@Component({
  selector: 'app-manager',
  templateUrl: './manager.component.html',
  styleUrls: ['./manager.component.css']
})
export class ManagerComponent implements OnInit {

  menuSelection = MenuSelection;
  private selection: MenuSelection;
  displayedUserName: string;
  currentUserName: string;

  constructor(
    private location: Location,
    private router: Router,
    private loginService: LoginService
  ) {

  }

  ngOnInit() {
    this.router.routeReuseStrategy.shouldReuseRoute = function() {
      return false;
    }

    this.router.events.subscribe(
      value => {
        if (value instanceof NavigationStart) {
          this.select(value.url.toString());
        }
      }
    );

    this.select(this.location.path().toString());
    this.currentUserName = this.loginService.getCurrentUserName();
    this.displayedUserName = this.currentUserName;
  }

  private select(url: string) {
    if (url.startsWith('/manager/clusters')) {
      this.selection = MenuSelection.Clusters;
    } else if (url.startsWith('/manager/library')) {
      this.selection = MenuSelection.Library;
    } else if (url.startsWith('/manager/resources')) {
      this.selection = MenuSelection.Resources;
    } else {
      this.selection = MenuSelection.Programs;
    }
  }

  isSelected(isSelected: MenuSelection): boolean {
    return this.selection === isSelected;
  }

  logout(): void {
    this.loginService.logout();
    this.router.navigate(['/login']);
  }

  changeUserName(): void {
    this.loginService.login(this.displayedUserName);
    this.router.navigate(['/']);
  }

}
