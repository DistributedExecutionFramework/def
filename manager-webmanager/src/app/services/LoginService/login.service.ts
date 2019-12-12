import {Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class LoginService {

  private userName: string = null;
  private readonly useStorage: boolean;
  private userNameKey = "username";

  constructor() {
    if (typeof(Storage) !== "undefined") {
      this.useStorage = true;
    } else {
      this.useStorage = false;
    }
  }

  isUserLoggedIn(): boolean {
    const currentUserName = this.fetchUserName();
    if (currentUserName !== null
      && currentUserName !== '') {
      return true;
    }
    return false;
  }

  isAdmin(): boolean {
    let userName = this.fetchUserName();
    if (userName && userName === 'defadmin') {
      return true;
    }
    return false;
  }

  login(userName: string): void {
    if (this.isUserNameValid(userName)) {
      this.storeUserName(userName);
    }
  }

  logout(): void {
    this.removeUserName();
  }

  getCurrentUserName(): string {
    return this.fetchUserName();
  }

  private isUserNameValid(userName): boolean {
    if (userName !== null
      && userName !== undefined
      && userName !== '') {
      return true;
    }
    return false;
  }

  private storeUserName(userName: string): void {
    if (this.useStorage) {
      localStorage.setItem(this.userNameKey, userName);
    } else {
      this.userName = userName;
    }
  }

  private fetchUserName(): string | null {
    if (this.useStorage) {
      const name =  localStorage.getItem(this.userNameKey);
      return name;
    } else {
      return this.userName;
    }
  }

  private removeUserName(): void {
    if (this.useStorage) {
      localStorage.removeItem(this.userNameKey);
    } else {
      this.userName = null;
    }
  }

}
