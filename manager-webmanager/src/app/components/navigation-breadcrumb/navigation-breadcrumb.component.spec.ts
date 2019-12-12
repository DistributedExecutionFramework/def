import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { NavigationBreadcrumbComponent } from './navigation-breadcrumb.component';

describe('NavigationBreadcrumbComponent', () => {
  let component: NavigationBreadcrumbComponent;
  let fixture: ComponentFixture<NavigationBreadcrumbComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ NavigationBreadcrumbComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(NavigationBreadcrumbComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
