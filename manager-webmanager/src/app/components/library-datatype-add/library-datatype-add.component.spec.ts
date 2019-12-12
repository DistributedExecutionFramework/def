import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { LibraryDatatypeAddComponent } from './library-datatype-add.component';

describe('LibraryDatatypeAddComponent', () => {
  let component: LibraryDatatypeAddComponent;
  let fixture: ComponentFixture<LibraryDatatypeAddComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ LibraryDatatypeAddComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(LibraryDatatypeAddComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
