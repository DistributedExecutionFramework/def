import {Component, OnInit} from '@angular/core';
import {LoginService} from "../../services/LoginService/login.service";
import {Router} from "@angular/router";

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {

  private defaultValue = "User name";
  userName: string;

  constructor(
    private loginService: LoginService,
    private router: Router
  ) { }

  ngOnInit() {
    this.setDefaultValue();
  }

  inputGetsFocus(): void {
    if (this.userName === this.defaultValue) {
      this.removeDefaultValue();
    }
  }

  inputLosesFocus(): void {
    if (this.userName === '') {
      this.setDefaultValue();
    }
  }

  private setDefaultValue(): void {
    this.userName = this.defaultValue;
  }

  private removeDefaultValue(): void {
    this.userName = '';
  }

  login(): void {
    if (this.userName !== this.defaultValue) {
      this.loginService.login(this.userName);
      this.router.navigate(['/manager']);
    }
  }
}
