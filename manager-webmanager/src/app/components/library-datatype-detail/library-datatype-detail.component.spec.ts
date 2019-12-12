import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { LibraryDatatypeDetailComponent } from './library-datatype-detail.component';

describe('LibraryDatatypeDetailComponent', () => {
  let component: LibraryDatatypeDetailComponent;
  let fixture: ComponentFixture<LibraryDatatypeDetailComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ LibraryDatatypeDetailComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(LibraryDatatypeDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
