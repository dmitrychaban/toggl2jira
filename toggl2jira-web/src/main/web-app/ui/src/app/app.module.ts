import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';


import { AppComponent } from './app.component';
import { SettingsComponent } from './settings/settings.component';
import { ConverterComponent } from './converter/converter.component';


const appRoutes: Routes = [
  {path: 'converter', component: ConverterComponent},
  {path: 'settings', component:  SettingsComponent}
];

@NgModule({
  declarations: [
    AppComponent,
    SettingsComponent,
    ConverterComponent
  ],
  imports: [
    BrowserModule,
    RouterModule.forRoot(
      appRoutes,
      { enableTracing: true } // <-- debugging purposes only
    )
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
